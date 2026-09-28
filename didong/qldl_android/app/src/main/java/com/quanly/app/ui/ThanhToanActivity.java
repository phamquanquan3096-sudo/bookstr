package com.quanly.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.BankInfo;
import com.quanly.app.model.CartItem;
import com.quanly.app.model.DonHang;
import com.quanly.app.model.OrderCreateRequest;
import com.quanly.app.model.OrderItemDto;
import com.quanly.app.utils.CartManager;
import com.quanly.app.utils.PaymentDialogHelper;
import com.quanly.app.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThanhToanActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etAddress;
    private TextView tvOrderSummaryList, tvSubTotal, tvTotalPayment;
    private Button btnSubmitPayment;

    // Payment method components
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbBank, rbWallet;
    private LinearLayout layoutOnlineBankingDetails;
    private Spinner spnCheckoutBank;
    private TextView tvCheckoutBankSummary;

    private List<BankInfo> bankList;
    private double currentTotal = 0.0;
    private String userId;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userId = getIntent().getStringExtra("userId");
        if ((userId == null || userId.isEmpty()) && SessionManager.isLoggedIn(this)) {
            userId = SessionManager.getUserId(this);
        }
        if (userId == null || userId.isEmpty()) {
            userId = "DL01";
        }

        initViews();
        setupBankSelector();
        bindOrderSummary();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etAddress = findViewById(R.id.etAddress);
        tvOrderSummaryList = findViewById(R.id.tvOrderSummaryList);
        tvSubTotal = findViewById(R.id.tvSubTotal);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);
        btnSubmitPayment = findViewById(R.id.btnSubmitPayment);

        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCOD = findViewById(R.id.rbCOD);
        rbBank = findViewById(R.id.rbBank);
        rbWallet = findViewById(R.id.rbWallet);
        layoutOnlineBankingDetails = findViewById(R.id.layoutOnlineBankingDetails);
        spnCheckoutBank = findViewById(R.id.spnCheckoutBank);
        tvCheckoutBankSummary = findViewById(R.id.tvCheckoutBankSummary);

        etAddress.setText("123 Nguyễn Văn Cừ, Q.5, TP.HCM");
    }

    private void setupBankSelector() {
        bankList = BankInfo.getDefaultBanks();
        ArrayAdapter<BankInfo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bankList);
        spnCheckoutBank.setAdapter(adapter);

        spnCheckoutBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BankInfo selected = bankList.get(position);
                updateBankSummaryView(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateBankSummaryView(BankInfo bank) {
        if (bank != null && tvCheckoutBankSummary != null) {
            String text = "Ngân hàng: " + bank.getBankName() + "\n"
                    + "Số TK: " + bank.getAccountNumber() + "\n"
                    + "Chủ TK: " + bank.getAccountName();
            tvCheckoutBankSummary.setText(text);
        }
    }

    private void bindOrderSummary() {
        List<CartItem> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.getSanPham() != null) {
                sb.append(i + 1).append(". ")
                  .append(item.getSanPham().getTenSP())
                  .append("  x").append(item.getSoLuong())
                  .append("  (").append(formatter.format(item.getThanhTien())).append("đ)\n");
                total += item.getThanhTien();
            }
        }

        currentTotal = total;
        tvOrderSummaryList.setText(sb.toString().trim());
        String totalStr = formatter.format(total) + "đ";
        tvSubTotal.setText(totalStr);
        tvTotalPayment.setText(totalStr);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCOD) {
                layoutOnlineBankingDetails.setVisibility(View.GONE);
                btnSubmitPayment.setText("Xác Nhận Đặt Hàng (COD)");
            } else if (checkedId == R.id.rbBank) {
                layoutOnlineBankingDetails.setVisibility(View.VISIBLE);
                btnSubmitPayment.setText("Xác Nhận & Quét Mã QR Thanh Toán");
                // Default to bank (e.g. MB or VCB)
                spnCheckoutBank.setSelection(0);
            } else if (checkedId == R.id.rbWallet) {
                layoutOnlineBankingDetails.setVisibility(View.VISIBLE);
                btnSubmitPayment.setText("Xác Nhận & Quét Mã Ví Điện Tử");
                // Select MoMo if available
                for (int i = 0; i < bankList.size(); i++) {
                    if ("MOMO".equalsIgnoreCase(bankList.get(i).getBankCode())) {
                        spnCheckoutBank.setSelection(i);
                        break;
                    }
                }
            }
        });

        btnSubmitPayment.setOnClickListener(v -> submitPayment());
    }

    private void submitPayment() {
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderItemDto> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.getSanPham() != null && item.getSanPham().getMaSP() != null) {
                orderItems.add(new OrderItemDto(item.getSanPham().getMaSP(), item.getSoLuong()));
            }
        }

        boolean isOnlinePayment = rbBank.isChecked() || rbWallet.isChecked();
        final BankInfo selectedBank = (BankInfo) spnCheckoutBank.getSelectedItem();

        btnSubmitPayment.setEnabled(false);
        OrderCreateRequest request = new OrderCreateRequest(userId, address, orderItems);
        ApiClient.getService().createDonHang(request).enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                btnSubmitPayment.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    DonHang donHang = response.body().getData();
                    String maDH = donHang != null && donHang.getMaDH() != null ? donHang.getMaDH() : ("DH" + System.currentTimeMillis() % 100000);
                    double totalAmount = (donHang != null && donHang.getTongTien() != null && donHang.getTongTien() > 0) ? donHang.getTongTien() : currentTotal;

                    CartManager.getInstance().clearCart();

                    if (isOnlinePayment) {
                        // Open Online Payment VietQR Dialog
                        PaymentDialogHelper.showQrPaymentDialog(ThanhToanActivity.this, maDH, totalAmount, selectedBank, new PaymentDialogHelper.PaymentCallback() {
                            @Override
                            public void onPaymentSuccess(String maDH) {
                                finish();
                            }

                            @Override
                            public void onPaymentLater(String maDH) {
                                Toast.makeText(ThanhToanActivity.this, "Đơn hàng " + maDH + " đã được tạo! Bạn có thể thanh toán sau trong Chi Tiết Đơn Hàng.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    } else {
                        Toast.makeText(ThanhToanActivity.this, "🎉 Đặt hàng thành công! Mã đơn: " + maDH, Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Đặt hàng thất bại";
                    Toast.makeText(ThanhToanActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                btnSubmitPayment.setEnabled(true);
                Toast.makeText(ThanhToanActivity.this, "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

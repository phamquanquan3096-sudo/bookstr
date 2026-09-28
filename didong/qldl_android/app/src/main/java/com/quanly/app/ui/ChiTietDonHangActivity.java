package com.quanly.app.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.gson.Gson;
import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.ChiTietDonHang;
import com.quanly.app.model.DonHang;
import com.quanly.app.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietDonHangActivity extends AppCompatActivity {

    private DonHang donHang;
    private String maDH;
    private String userId, userType, openAction;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private ImageView btnBack, btnRefresh;
    private TextView tvToolbarTitle, tvDetailMaDH, tvDetailTrangThai, tvDetailMaDL, tvDetailDiemGiao;
    private TextView tvDetailNgayLap, tvDetailTinhTrangGH, tvDetailThanhToan, tvDetailTongTien, tvOrderDetailContent;
    private Button btnApproveOrder, btnConfirmDelivery, btnConfirmPayment, btnCancelOrder, btnReturnToList;
    private Dialog activeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_don_hang);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        donHang = (DonHang) getIntent().getSerializableExtra("donHang");
        maDH = getIntent().getStringExtra("maDH");
        if (maDH == null && donHang != null) {
            maDH = donHang.getMaDH();
        }
        if (maDH != null) {
            maDH = maDH.trim();
        }

        userId = getIntent().getStringExtra("userId");
        userType = getIntent().getStringExtra("userType");
        openAction = getIntent().getStringExtra("openAction");

        if ((userId == null || userId.isEmpty()) && SessionManager.isLoggedIn(this)) {
            userId = SessionManager.getUserId(this);
            userType = SessionManager.getUserType(this);
        }

        initViews();
        setupListeners();
        renderOrderData();
        loadOrderDetailFromApi();

        if (openAction != null) {
            handleInitialAction(openAction);
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (activeDialog != null && activeDialog.isShowing()) {
            try {
                activeDialog.dismiss();
            } catch (Exception ignored) {}
        }
        openAction = intent.getStringExtra("openAction");
        String newMaDH = intent.getStringExtra("maDH");
        if (newMaDH != null && !newMaDH.isEmpty()) {
            maDH = newMaDH.trim();
        }
        donHang = (DonHang) intent.getSerializableExtra("donHang");
        renderOrderData();
        loadOrderDetailFromApi();
        if (openAction != null) {
            handleInitialAction(openAction);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvDetailMaDH = findViewById(R.id.tvDetailMaDH);
        tvDetailTrangThai = findViewById(R.id.tvDetailTrangThai);
        tvDetailMaDL = findViewById(R.id.tvDetailMaDL);
        tvDetailDiemGiao = findViewById(R.id.tvDetailDiemGiao);
        tvDetailNgayLap = findViewById(R.id.tvDetailNgayLap);
        tvDetailTinhTrangGH = findViewById(R.id.tvDetailTinhTrangGH);
        tvDetailThanhToan = findViewById(R.id.tvDetailThanhToan);
        tvDetailTongTien = findViewById(R.id.tvDetailTongTien);
        tvOrderDetailContent = findViewById(R.id.tvOrderDetailContent);

        btnApproveOrder = findViewById(R.id.btnApproveOrder);
        btnConfirmDelivery = findViewById(R.id.btnConfirmDelivery);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnReturnToList = findViewById(R.id.btnReturnToList);

        if (maDH != null) {
            tvToolbarTitle.setText("📋 Đơn Hàng #" + maDH);
            tvDetailMaDH.setText("Mã ĐH: " + maDH);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadOrderDetailFromApi());
        if (btnReturnToList != null) {
            btnReturnToList.setOnClickListener(v -> finish());
        }

        btnApproveOrder.setOnClickListener(v -> showApproveDialog());
        btnConfirmDelivery.setOnClickListener(v -> showDeliveryDialog());
        btnConfirmPayment.setOnClickListener(v -> showPaymentDialog());
        btnCancelOrder.setOnClickListener(v -> showCancelDialog());
    }

    private void renderOrderData() {
        if (donHang == null) return;

        String trangThai = donHang.getTrangThai() != null ? donHang.getTrangThai() : "Mới tạo";
        tvDetailTrangThai.setText(trangThai);

        boolean isFinalized = "Đã hủy".equalsIgnoreCase(trangThai) || "Từ chối".equalsIgnoreCase(trangThai);
        boolean isApproved = "Đã duyệt".equalsIgnoreCase(trangThai) || "Đã xét duyệt".equalsIgnoreCase(trangThai) || "Đã giao".equalsIgnoreCase(trangThai);
        boolean isDelivered = "Đã giao".equalsIgnoreCase(donHang.getTinhTrangGH());
        boolean isPaid = "Đã thanh toán".equalsIgnoreCase(donHang.getTinhTrangThanhToan());

        if (isFinalized) {
            tvDetailTrangThai.setBackgroundColor(Color.parseColor("#E53935"));
        } else if (isApproved) {
            tvDetailTrangThai.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            tvDetailTrangThai.setBackgroundColor(Color.parseColor("#1E88E5"));
        }

        tvDetailMaDL.setText("👤 Khách hàng / Đại lý: " + (donHang.getMaDL() != null ? donHang.getMaDL() : "DL01"));
        tvDetailDiemGiao.setText("📍 Địa chỉ giao hàng: " + (donHang.getDiemGiao() != null ? donHang.getDiemGiao() : "Chưa cập nhật"));
        tvDetailNgayLap.setText("🕒 Ngày đặt hàng: " + (donHang.getNgayLap() != null ? donHang.getNgayLap() : "Vừa xong"));

        tvDetailTinhTrangGH.setText("🚚 Vận chuyển: " + (donHang.getTinhTrangGH() != null ? donHang.getTinhTrangGH() : "Chờ giao"));
        
        String tt = donHang.getTinhTrangThanhToan() != null ? donHang.getTinhTrangThanhToan() : "Chưa thanh toán";
        tvDetailThanhToan.setText("💵 " + tt);
        tvDetailThanhToan.setTextColor("Đã thanh toán".equalsIgnoreCase(tt) ? Color.parseColor("#4CAF50") : Color.parseColor("#E53935"));

        tvDetailTongTien.setText((donHang.getTongTien() != null ? formatter.format(donHang.getTongTien()) : "0") + " VNĐ");

        if (userType == null || userType.isEmpty()) {
            userType = SessionManager.getUserType(this);
        }

        boolean isAdmin = userType != null && (userType.contains("QTHT") || userType.equalsIgnoreCase("ADMIN"));
        boolean isKinhDoanh = userType != null && userType.contains("NVKD");
        boolean isKeToan = userType != null && (userType.contains("NVKT") || userType.contains("KE_TOAN"));
        boolean isKho = userType != null && (userType.contains("NVK") || userType.contains("KHO") || userType.contains("THU_KHO"));
        boolean isGiaoHang = userType != null && (userType.contains("NVGH") || userType.contains("GIAO_HANG"));
        boolean isStaff = isAdmin || isKinhDoanh || isKeToan || isKho || isGiaoHang || (userType != null && (userType.contains("STAFF") || userType.contains("NV")));
        boolean isCustomer = !isStaff;

        // Cập nhật trạng thái hiển thị các nút thao tác theo vai trò
        if (isCustomer) {
            // Khách hàng: Chỉ có chức năng Hủy đơn (khi chưa giao và chưa hủy)
            btnApproveOrder.setVisibility(View.GONE);
            btnConfirmPayment.setVisibility(View.GONE);
            btnConfirmDelivery.setVisibility(View.GONE);
            btnCancelOrder.setVisibility(!isFinalized && !isDelivered ? View.VISIBLE : View.GONE);
        } else if (isKeToan) {
            // Đối với Nhân viên Kế toán: Chỉ hiện mục Thu tiền (Đã thu tiền / Chưa thu tiền), không hiện Duyệt đơn, Giao hàng, Hủy đơn
            btnApproveOrder.setVisibility(View.GONE);
            btnConfirmDelivery.setVisibility(View.GONE);
            btnCancelOrder.setVisibility(View.GONE);
            btnConfirmPayment.setVisibility(!isPaid && !isFinalized ? View.VISIBLE : View.GONE);
        } else if (isGiaoHang) {
            // Nhân viên Giao hàng: Chỉ hiện nút Giao hàng
            btnApproveOrder.setVisibility(View.GONE);
            btnConfirmPayment.setVisibility(View.GONE);
            btnCancelOrder.setVisibility(View.GONE);
            btnConfirmDelivery.setVisibility(isApproved && !isDelivered && !isFinalized ? View.VISIBLE : View.GONE);
        } else {
            // Admin / Kinh doanh / Kho
            btnApproveOrder.setVisibility(!isApproved && !isFinalized ? View.VISIBLE : View.GONE);
            btnConfirmDelivery.setVisibility(isApproved && !isDelivered && !isFinalized ? View.VISIBLE : View.GONE);
            btnConfirmPayment.setVisibility(!isPaid && !isFinalized ? View.VISIBLE : View.GONE);
            btnCancelOrder.setVisibility(!isFinalized && !isDelivered ? View.VISIBLE : View.GONE);
        }
    }

    private void loadOrderDetailFromApi() {
        if (maDH == null || maDH.isEmpty()) return;

        tvOrderDetailContent.setText("⏳ Đang tải chi tiết các sản phẩm từ máy chủ...");

        ApiClient.getApiService().getOrderDetail(maDH).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Map<String, Object> data = response.body().getData();
                    
                    // Parse Order Object if available
                    Object dhObj = data.get("donHang");
                    Gson gson = new Gson();
                    if (dhObj != null) {
                        try {
                            String dhJson = gson.toJson(dhObj);
                            DonHang liveDH = gson.fromJson(dhJson, DonHang.class);
                            if (liveDH != null) {
                                donHang = liveDH;
                                renderOrderData();
                            }
                        } catch (Exception ignored) {}
                    }

                    // Parse Items
                    Object chiTietObj = data.get("chiTiet");
                    List<ChiTietDonHang> ctList = new ArrayList<>();
                    if (chiTietObj instanceof List) {
                        List<?> itemList = (List<?>) chiTietObj;
                        for (Object itemObj : itemList) {
                            try {
                                String json = gson.toJson(itemObj);
                                ChiTietDonHang ct = gson.fromJson(json, ChiTietDonHang.class);
                                if (ct != null) {
                                    ctList.add(ct);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    tvOrderDetailContent.setText(buildProductListText(ctList));
                } else {
                    String msg = (response.body() != null && response.body().getMessage() != null) ? response.body().getMessage() : "Không thể tải chi tiết sản phẩm!";
                    tvOrderDetailContent.setText("⚠️ " + msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                tvOrderDetailContent.setText("❌ Lỗi kết nối máy chủ: " + t.getMessage());
            }
        });
    }

    private String buildProductListText(List<ChiTietDonHang> items) {
        if (items == null || items.isEmpty()) {
            return "(Đơn hàng này chưa có thông tin sản phẩm chi tiết)";
        }
        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (ChiTietDonHang ct : items) {
            String tenSP = ct.getTenSP() != null && !ct.getTenSP().trim().isEmpty() ? ct.getTenSP() : ("Mã SP: " + ct.getMaSP());
            String donGiaStr = ct.getDonGia() != null ? formatter.format(ct.getDonGia()) + "đ" : "--";
            String thanhTienStr = ct.getThanhTien() != null ? formatter.format(ct.getThanhTien()) + "đ" : "--";

            sb.append(count++).append(". ").append(tenSP)
              .append("\n    • Số lượng: ").append(ct.getSoLuong() != null ? ct.getSoLuong() : 1)
              .append("  |  Đơn giá: ").append(donGiaStr)
              .append("\n    • Thành tiền: ").append(thanhTienStr).append("\n\n");
        }
        return sb.toString().trim();
    }

    private void handleInitialAction(String action) {
        if ("APPROVE".equalsIgnoreCase(action)) {
            showApproveDialog();
        } else if ("DELIVERY".equalsIgnoreCase(action)) {
            showDeliveryDialog();
        } else if ("PAYMENT".equalsIgnoreCase(action)) {
            showPaymentDialog();
        } else if ("CANCEL".equalsIgnoreCase(action)) {
            showCancelDialog();
        }
    }

    private Dialog createCustomDialog(View dialogView) {
        if (activeDialog != null && activeDialog.isShowing()) {
            try {
                activeDialog.dismiss();
            } catch (Exception ignored) {}
        }
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(d -> {
            if (openAction != null) {
                finish();
            }
        });
        dialog.setOnDismissListener(d -> {
            activeDialog = null;
        });
        activeDialog = dialog;
        return dialog;
    }

    @Override
    public void onBackPressed() {
        if (activeDialog != null && activeDialog.isShowing()) {
            try {
                activeDialog.dismiss();
                activeDialog = null;
            } catch (Exception ignored) {}
        }
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (activeDialog != null && activeDialog.isShowing()) {
            try {
                activeDialog.dismiss();
            } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    // ====================================================
    // CỬA SỔ PHÊ DUYỆT ĐƠN HÀNG
    // ====================================================
    private void showApproveDialog() {
        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_approve_order, null);
            TextView tvApproveMaDH = dialogView.findViewById(R.id.tvApproveMaDH);
            TextView tvApproveMaDL = dialogView.findViewById(R.id.tvApproveMaDL);
            TextView tvApproveTongTien = dialogView.findViewById(R.id.tvApproveTongTien);
            TextView tvApproveDiemGiao = dialogView.findViewById(R.id.tvApproveDiemGiao);
            Button btnApproveClose = dialogView.findViewById(R.id.btnApproveClose);
            Button btnConfirmApproveSubmit = dialogView.findViewById(R.id.btnConfirmApproveSubmit);
            ImageView btnApproveDialogClose = dialogView.findViewById(R.id.btnApproveDialogClose);

            if (tvApproveMaDH != null) tvApproveMaDH.setText("Mã ĐH: " + maDH);
            if (donHang != null) {
                if (tvApproveMaDL != null) tvApproveMaDL.setText("Khách hàng / Đại lý: " + (donHang.getMaDL() != null ? donHang.getMaDL() : "DL01"));
                if (tvApproveTongTien != null) tvApproveTongTien.setText("Tổng tiền: " + (donHang.getTongTien() != null ? formatter.format(donHang.getTongTien()) : "0") + " VNĐ");
                if (tvApproveDiemGiao != null) tvApproveDiemGiao.setText("Điểm giao: " + (donHang.getDiemGiao() != null ? donHang.getDiemGiao() : "Chưa cập nhật"));
            }

            Dialog dialog = createCustomDialog(dialogView);
            if (btnApproveDialogClose != null) {
                btnApproveDialogClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (openAction != null) finish();
                });
            }
            if (btnApproveClose != null) {
                btnApproveClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (openAction != null) finish();
                });
            }

            if (btnConfirmApproveSubmit != null) {
                btnConfirmApproveSubmit.setOnClickListener(v -> {
                    dialog.dismiss();
                    doApproveOrder();
                });
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            doApproveOrder();
        }
    }

    private void doApproveOrder() {
        Toast.makeText(this, "⏳ Đang phê duyệt đơn hàng " + maDH + "...", Toast.LENGTH_SHORT).show();
        ApiClient.getService().approveOrder(maDH).enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ChiTietDonHangActivity.this, "🎉 Phê duyệt thành công đơn hàng " + maDH + "!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    if (donHang != null) {
                        donHang.setTrangThai("Đã duyệt");
                        donHang.setTinhTrangGH("Đang giao");
                    }
                    renderOrderData();
                    if (openAction != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (!isFinishing()) {
                                finish();
                            }
                        }, 500);
                    } else {
                        loadOrderDetailFromApi();
                    }
                } else {
                    String msg = (response.body() != null && response.body().getMessage() != null) ? response.body().getMessage() : "Không thể phê duyệt đơn hàng!";
                    Toast.makeText(ChiTietDonHangActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                Toast.makeText(ChiTietDonHangActivity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ====================================================
    // CỬA SỔ XÁC NHẬN GIAO HÀNG
    // ====================================================
    private void showDeliveryDialog() {
        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delivery_order, null);
            TextView tvDeliveryMaDH = dialogView.findViewById(R.id.tvDeliveryMaDH);
            TextView tvDeliveryMaDL = dialogView.findViewById(R.id.tvDeliveryMaDL);
            TextView tvDeliveryDiemGiao = dialogView.findViewById(R.id.tvDeliveryDiemGiao);
            Button btnDeliveryClose = dialogView.findViewById(R.id.btnDeliveryClose);
            Button btnConfirmDeliverySubmit = dialogView.findViewById(R.id.btnConfirmDeliverySubmit);
            ImageView btnDeliveryDialogClose = dialogView.findViewById(R.id.btnDeliveryDialogClose);

            if (tvDeliveryMaDH != null) tvDeliveryMaDH.setText("Mã ĐH: " + maDH);
            if (donHang != null) {
                if (tvDeliveryMaDL != null) tvDeliveryMaDL.setText("Người nhận / Đại lý: " + (donHang.getMaDL() != null ? donHang.getMaDL() : "DL01"));
                if (tvDeliveryDiemGiao != null) tvDeliveryDiemGiao.setText("📍 Địa chỉ giao: " + (donHang.getDiemGiao() != null ? donHang.getDiemGiao() : "Chưa cập nhật"));
            }

            Dialog dialog = createCustomDialog(dialogView);
            if (btnDeliveryDialogClose != null) {
                btnDeliveryDialogClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (openAction != null) finish();
                });
            }
            if (btnDeliveryClose != null) {
                btnDeliveryClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (openAction != null) finish();
                });
            }

            if (btnConfirmDeliverySubmit != null) {
                btnConfirmDeliverySubmit.setOnClickListener(v -> {
                    dialog.dismiss();
                    doConfirmDelivery();
                });
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            doConfirmDelivery();
        }
    }

    private void doConfirmDelivery() {
        Toast.makeText(this, "⏳ Đang cập nhật trạng thái đã giao...", Toast.LENGTH_SHORT).show();
        ApiClient.getApiService().confirmDelivery(maDH).enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ChiTietDonHangActivity.this, "🎉 Đã xác nhận giao hàng " + maDH + " thành công!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    if (donHang != null) {
                        donHang.setTinhTrangGH("Đã giao");
                        donHang.setTrangThai("Đã giao");
                    }
                    renderOrderData();
                    if (openAction != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (!isFinishing()) {
                                finish();
                            }
                        }, 500);
                    } else {
                        loadOrderDetailFromApi();
                    }
                } else {
                    Toast.makeText(ChiTietDonHangActivity.this, "❌ Không thể cập nhật giao hàng!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                Toast.makeText(ChiTietDonHangActivity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ====================================================
    // CỬA SỔ XÁC NHẬN THU TIỀN / THANH TOÁN
    // ====================================================
    private void showPaymentDialog() {
        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment_order, null);
            TextView tvPaymentMaDH = dialogView.findViewById(R.id.tvPaymentMaDH);
            TextView tvPaymentMaDL = dialogView.findViewById(R.id.tvPaymentMaDL);
            TextView tvPaymentAmount = dialogView.findViewById(R.id.tvPaymentAmount);
            Button btnPaymentClose = dialogView.findViewById(R.id.btnPaymentClose);
            Button btnConfirmPaymentSubmit = dialogView.findViewById(R.id.btnConfirmPaymentSubmit);
            ImageView btnPaymentDialogClose = dialogView.findViewById(R.id.btnPaymentDialogClose);

            if (tvPaymentMaDH != null) tvPaymentMaDH.setText("Mã ĐH: " + maDH);
            if (donHang != null) {
                if (tvPaymentMaDL != null) tvPaymentMaDL.setText("Khách hàng / Đại lý: " + (donHang.getMaDL() != null ? donHang.getMaDL() : "DL01"));
                if (tvPaymentAmount != null) tvPaymentAmount.setText((donHang.getTongTien() != null ? formatter.format(donHang.getTongTien()) : "0") + " VNĐ");
            }

            Dialog dialog = createCustomDialog(dialogView);
            if (btnPaymentDialogClose != null) {
                btnPaymentDialogClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (openAction != null) finish();
                });
            }
            if (btnPaymentClose != null) {
                btnPaymentClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (openAction != null) finish();
                });
            }

            RadioButton rbPayTransfer = dialogView.findViewById(R.id.rbPayTransfer);

            if (btnConfirmPaymentSubmit != null) {
                btnConfirmPaymentSubmit.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (rbPayTransfer != null && rbPayTransfer.isChecked()) {
                        double amt = donHang != null && donHang.getTongTien() != null ? donHang.getTongTien() : 0.0;
                        com.quanly.app.utils.PaymentDialogHelper.showQrPaymentDialog(ChiTietDonHangActivity.this, maDH, amt, null, new com.quanly.app.utils.PaymentDialogHelper.PaymentCallback() {
                            @Override
                            public void onPaymentSuccess(String paidMaDH) {
                                if (donHang != null) {
                                    donHang.setTinhTrangThanhToan("Đã thanh toán");
                                }
                                renderOrderData();
                                if (openAction != null) {
                                    finish();
                                } else {
                                    loadOrderDetailFromApi();
                                }
                            }

                            @Override
                            public void onPaymentLater(String paidMaDH) {
                            }
                        });
                    } else {
                        doConfirmPayment();
                    }
                });
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            doConfirmPayment();
        }
    }

    private void doConfirmPayment() {
        Toast.makeText(this, "⏳ Đang ghi nhận thu tiền đơn hàng " + maDH + "...", Toast.LENGTH_SHORT).show();
        ApiClient.getApiService().confirmPayment(maDH).enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ChiTietDonHangActivity.this, "💵 Đã ghi nhận thu tiền đơn hàng " + maDH + " thành công!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    if (donHang != null) {
                        donHang.setTinhTrangThanhToan("Đã thanh toán");
                    }
                    renderOrderData();
                    if (openAction != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (!isFinishing()) {
                                finish();
                            }
                        }, 500);
                    } else {
                        loadOrderDetailFromApi();
                    }
                } else {
                    Toast.makeText(ChiTietDonHangActivity.this, "❌ Không thể cập nhật thanh toán!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                Toast.makeText(ChiTietDonHangActivity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ====================================================
    // CỬA SỔ XỬ LÝ HỦY ĐƠN HÀNG
    // ====================================================
    private void showCancelDialog() {
        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_order, null);
            TextView tvCancelMaDH = dialogView.findViewById(R.id.tvCancelMaDH);
            TextView tvCancelTongTien = dialogView.findViewById(R.id.tvCancelTongTien);
            TextView tvCancelDiemGiao = dialogView.findViewById(R.id.tvCancelDiemGiao);
            RadioGroup rgCancelReason = dialogView.findViewById(R.id.rgCancelReason);
            EditText etCancelNote = dialogView.findViewById(R.id.etCancelNote);
            Button btnCancelClose = dialogView.findViewById(R.id.btnCancelClose);
            Button btnConfirmCancelSubmit = dialogView.findViewById(R.id.btnConfirmCancelSubmit);
            ImageView btnCancelDialogClose = dialogView.findViewById(R.id.btnCancelDialogClose);

            if (tvCancelMaDH != null) tvCancelMaDH.setText("Mã ĐH: " + maDH);
            if (donHang != null) {
                if (tvCancelTongTien != null) tvCancelTongTien.setText("Tổng giá trị: " + (donHang.getTongTien() != null ? formatter.format(donHang.getTongTien()) : "0") + " VNĐ");
                if (tvCancelDiemGiao != null) tvCancelDiemGiao.setText("Điểm giao: " + (donHang.getDiemGiao() != null ? donHang.getDiemGiao() : "Chưa cập nhật"));
            }

            Dialog dialog = createCustomDialog(dialogView);
            if (btnCancelDialogClose != null) {
                btnCancelDialogClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (openAction != null) finish();
                });
            }
            if (btnCancelClose != null) {
                btnCancelClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (openAction != null) finish();
                });
            }

            if (btnConfirmCancelSubmit != null) {
                btnConfirmCancelSubmit.setOnClickListener(v -> {
                    String reason = "Khách hàng đổi ý";
                    if (rgCancelReason != null) {
                        int selectedId = rgCancelReason.getCheckedRadioButtonId();
                        RadioButton rb = dialogView.findViewById(selectedId);
                        if (rb != null) {
                            reason = rb.getText().toString();
                        }
                    }
                    String note = etCancelNote != null ? etCancelNote.getText().toString().trim() : "";
                    if (!note.isEmpty()) {
                        reason += " (" + note + ")";
                    }
                    dialog.dismiss();
                    doCancelOrder(reason);
                });
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            doCancelOrder("Khách hàng yêu cầu hủy");
        }
    }

    private void doCancelOrder(String reason) {
        Toast.makeText(this, "⏳ Đang tiến hành hủy đơn hàng " + maDH + "...", Toast.LENGTH_SHORT).show();
        ApiClient.getService().updateOrderStatus(maDH, "Đã hủy").enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ChiTietDonHangActivity.this, "✅ Đã hủy thành công đơn hàng " + maDH + "!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    if (donHang != null) {
                        donHang.setTrangThai("Đã hủy");
                    }
                    renderOrderData();
                    if (openAction != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (!isFinishing()) {
                                finish();
                            }
                        }, 500);
                    } else {
                        loadOrderDetailFromApi();
                    }
                } else {
                    String msg = (response.body() != null && response.body().getMessage() != null) ? response.body().getMessage() : "Không thể hủy đơn hàng!";
                    Toast.makeText(ChiTietDonHangActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                Toast.makeText(ChiTietDonHangActivity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

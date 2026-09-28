package com.quanly.app.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.BankInfo;
import com.quanly.app.model.DonHang;

import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentDialogHelper {

    public interface PaymentCallback {
        void onPaymentSuccess(String maDH);
        void onPaymentLater(String maDH);
    }

    public static void showQrPaymentDialog(Activity activity, String maDH, double amount, BankInfo initialBank, PaymentCallback callback) {
        if (activity == null || activity.isFinishing()) return;

        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedAmount = formatter.format(amount) + "đ";
        List<BankInfo> bankList = BankInfo.getDefaultBanks();

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_online_payment_qr, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout((int) (activity.getResources().getDisplayMetrics().widthPixels * 0.95), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.setCancelable(false);

        TextView tvQrMaDH = dialogView.findViewById(R.id.tvQrMaDH);
        TextView tvQrAmount = dialogView.findViewById(R.id.tvQrAmount);
        ImageView btnCopyAmount = dialogView.findViewById(R.id.btnCopyAmount);
        Spinner spnDialogBank = dialogView.findViewById(R.id.spnDialogBank);
        ImageView ivVietQrCode = dialogView.findViewById(R.id.ivVietQrCode);
        ProgressBar pbQrLoading = dialogView.findViewById(R.id.pbQrLoading);
        TextView tvDialogBankName = dialogView.findViewById(R.id.tvDialogBankName);
        TextView tvDialogAccountNo = dialogView.findViewById(R.id.tvDialogAccountNo);
        Button btnCopyAccountNo = dialogView.findViewById(R.id.btnCopyAccountNo);
        TextView tvDialogAccountName = dialogView.findViewById(R.id.tvDialogAccountName);
        TextView tvDialogTransferContent = dialogView.findViewById(R.id.tvDialogTransferContent);
        Button btnCopyContent = dialogView.findViewById(R.id.btnCopyContent);
        Button btnCopyAllInfo = dialogView.findViewById(R.id.btnCopyAllInfo);
        Button btnOpenBankingApp = dialogView.findViewById(R.id.btnOpenBankingApp);
        Button btnPayLater = dialogView.findViewById(R.id.btnPayLater);
        Button btnConfirmPaid = dialogView.findViewById(R.id.btnConfirmPaid);
        ImageView btnQrDialogClose = dialogView.findViewById(R.id.btnQrDialogClose);

        tvQrMaDH.setText("Mã đơn hàng: " + (maDH != null ? maDH : "DH---"));
        tvQrAmount.setText(formattedAmount);

        final String transferContent = (maDH != null ? maDH : "DH") + " Thanh toan sach";
        tvDialogTransferContent.setText(transferContent);

        // Setup Bank Spinner
        ArrayAdapter<BankInfo> bankAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, bankList);
        spnDialogBank.setAdapter(bankAdapter);

        int initialIndex = 0;
        if (initialBank != null) {
            for (int i = 0; i < bankList.size(); i++) {
                if (bankList.get(i).getBankCode().equalsIgnoreCase(initialBank.getBankCode())) {
                    initialIndex = i;
                    break;
                }
            }
        }
        spnDialogBank.setSelection(initialIndex);

        spnDialogBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BankInfo currentBank = bankList.get(position);
                tvDialogBankName.setText(currentBank.getBankName());
                tvDialogAccountNo.setText(currentBank.getAccountNumber());
                tvDialogAccountName.setText(currentBank.getAccountName());

                // Load VietQR code
                String qrUrl = currentBank.getVietQrUrl(maDH, amount);
                pbQrLoading.setVisibility(View.VISIBLE);
                Glide.with(activity)
                        .load(qrUrl)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                pbQrLoading.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                pbQrLoading.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(ivVietQrCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Copy buttons
        btnCopyAmount.setOnClickListener(v -> copyToClipboard(activity, "Số tiền", String.valueOf(Math.round(amount))));
        btnCopyAccountNo.setOnClickListener(v -> {
            BankInfo selected = (BankInfo) spnDialogBank.getSelectedItem();
            if (selected != null) {
                copyToClipboard(activity, "Số tài khoản", selected.getAccountNumber());
            }
        });
        btnCopyContent.setOnClickListener(v -> copyToClipboard(activity, "Nội dung CK", transferContent));

        btnCopyAllInfo.setOnClickListener(v -> {
            BankInfo selected = (BankInfo) spnDialogBank.getSelectedItem();
            if (selected != null) {
                String allInfo = "NGÂN HÀNG: " + selected.getBankName() + "\n"
                        + "SỐ TÀI KHOẢN: " + selected.getAccountNumber() + "\n"
                        + "CHỦ TÀI KHOẢN: " + selected.getAccountName() + "\n"
                        + "SỐ TIỀN: " + formattedAmount + "\n"
                        + "NỘI DUNG CHUYỂN KHOẢN: " + transferContent;
                copyToClipboard(activity, "Thông tin chuyển khoản", allInfo);
            }
        });

        // Open banking app intent
        btnOpenBankingApp.setOnClickListener(v -> {
            BankInfo selected = (BankInfo) spnDialogBank.getSelectedItem();
            boolean opened = false;
            if (selected != null && selected.getAppPackage() != null) {
                try {
                    Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(selected.getAppPackage());
                    if (launchIntent != null) {
                        activity.startActivity(launchIntent);
                        opened = true;
                    }
                } catch (Exception ignored) {}
            }
            if (!opened) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    activity.startActivity(Intent.createChooser(intent, "Chọn ứng dụng thanh toán"));
                } catch (Exception e) {
                    Toast.makeText(activity, "Vui lòng mở ứng dụng Ngân hàng của bạn để quét mã QR", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Close / Pay later
        View.OnClickListener closeAction = v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onPaymentLater(maDH);
            }
        };
        btnQrDialogClose.setOnClickListener(closeAction);
        btnPayLater.setOnClickListener(closeAction);

        // Confirm Paid
        btnConfirmPaid.setOnClickListener(v -> {
            btnConfirmPaid.setEnabled(false);
            btnConfirmPaid.setText("⏳ Đang xác nhận...");

            ApiClient.getApiService().confirmPayment(maDH).enqueue(new Callback<ApiResponse<DonHang>>() {
                @Override
                public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                    dialog.dismiss();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(activity, "🎉 Thanh toán thành công đơn hàng " + maDH + "!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(activity, "✅ Đã ghi nhận chuyển khoản cho đơn hàng " + maDH, Toast.LENGTH_LONG).show();
                    }
                    if (callback != null) {
                        callback.onPaymentSuccess(maDH);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                    dialog.dismiss();
                    Toast.makeText(activity, "✅ Đã ghi nhận chuyển khoản cho đơn hàng " + maDH, Toast.LENGTH_LONG).show();
                    if (callback != null) {
                        callback.onPaymentSuccess(maDH);
                    }
                }
            });
        });

        dialog.show();
    }

    private static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Đã sao chép " + label + " vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show();
        }
    }
}

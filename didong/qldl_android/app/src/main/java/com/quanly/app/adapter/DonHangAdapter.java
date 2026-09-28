package com.quanly.app.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.DonHang;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonHangAdapter extends RecyclerView.Adapter<DonHangAdapter.ViewHolder> {

    private List<DonHang> list = new ArrayList<>();
    private String userType;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private Activity getActivityFromContext(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        }
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private Dialog buildCustomDialog(Context context, View dialogView) {
        Activity activity = getActivityFromContext(context);
        Context safeContext = (activity != null && !activity.isFinishing()) ? activity : context;
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(safeContext);
        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        return dialog;
    }

    public void setList(List<DonHang> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setUserType(String userType) {
        this.userType = userType;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_don_hang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonHang dh = list.get(position);
        Context context = holder.itemView.getContext();

        if (userType == null || userType.isEmpty()) {
            userType = com.quanly.app.utils.SessionManager.getUserType(context);
        }

        boolean isAdmin = userType != null && (userType.contains("QTHT") || userType.equalsIgnoreCase("ADMIN"));
        boolean isKinhDoanh = userType != null && userType.contains("NVKD");
        boolean isKeToan = userType != null && (userType.contains("NVKT") || userType.contains("KE_TOAN"));
        boolean isKho = userType != null && (userType.contains("NVK") || userType.contains("KHO") || userType.contains("THU_KHO"));
        boolean isGiaoHang = userType != null && (userType.contains("NVGH") || userType.contains("GIAO_HANG"));
        boolean isStaff = isAdmin || isKinhDoanh || isKeToan || isKho || isGiaoHang || (userType != null && (userType.contains("STAFF") || userType.contains("NV")));
        boolean isCustomer = !isStaff;

        String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
        holder.tvMaDH.setText("Mã ĐH: " + maDH);

        String trangThai = dh.getTrangThai() != null ? dh.getTrangThai() : "Mới tạo";
        holder.tvTrangThai.setText(trangThai);

        boolean isFinalized = "Đã hủy".equalsIgnoreCase(trangThai) || "Từ chối".equalsIgnoreCase(trangThai);
        boolean isApproved = "Đã duyệt".equalsIgnoreCase(trangThai) || "Đã xét duyệt".equalsIgnoreCase(trangThai) || "Đã giao".equalsIgnoreCase(trangThai);
        boolean isDelivered = "Đã giao".equalsIgnoreCase(dh.getTinhTrangGH());
        boolean isPaid = "Đã thanh toán".equalsIgnoreCase(dh.getTinhTrangThanhToan());

        if (isFinalized) {
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#E53935"));
        } else if (isApproved) {
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#1E88E5"));
        }

        if (isCustomer) {
            // Khách hàng: Chỉ xem chi tiết và hủy đơn khi chưa giao
            holder.btnApproveOrder.setVisibility(View.GONE);
            holder.btnConfirmPayment.setVisibility(View.GONE);
            holder.btnConfirmDelivery.setVisibility(View.GONE);
            holder.btnCancelOrder.setVisibility(!isFinalized && !isDelivered ? View.VISIBLE : View.GONE);
        } else if (isKeToan) {
            // Đối với Nhân viên Kế toán: Chỉ hiện mục Thu tiền (Đã thu tiền / Chưa thu tiền), không hiện Duyệt đơn, Giao hàng, Hủy đơn
            holder.btnApproveOrder.setVisibility(View.GONE);
            holder.btnConfirmDelivery.setVisibility(View.GONE);
            holder.btnCancelOrder.setVisibility(View.GONE);
            holder.btnConfirmPayment.setVisibility(!isPaid && !isFinalized ? View.VISIBLE : View.GONE);
        } else if (isGiaoHang) {
            // Giao hàng: Chỉ hiện nút Giao hàng
            holder.btnApproveOrder.setVisibility(View.GONE);
            holder.btnConfirmPayment.setVisibility(View.GONE);
            holder.btnCancelOrder.setVisibility(View.GONE);
            holder.btnConfirmDelivery.setVisibility(isApproved && !isDelivered && !isFinalized ? View.VISIBLE : View.GONE);
        } else {
            // Quản trị viên / Kinh doanh / Kho
            holder.btnApproveOrder.setVisibility(!isApproved && !isFinalized ? View.VISIBLE : View.GONE);
            holder.btnCancelOrder.setVisibility(!isFinalized && !isDelivered ? View.VISIBLE : View.GONE);
            holder.btnConfirmDelivery.setVisibility(isApproved && !isDelivered && !isFinalized ? View.VISIBLE : View.GONE);
            holder.btnConfirmPayment.setVisibility(!isPaid && !isFinalized ? View.VISIBLE : View.GONE);
        }

        holder.tvNgayLap.setText("Mã KH: " + (dh.getMaDL() != null ? dh.getMaDL() : "DL01"));
        holder.tvDiemGiao.setText("📍 Điểm giao: " + (dh.getDiemGiao() != null ? dh.getDiemGiao() : "Chưa cập nhật"));

        String tongTienStr = dh.getTongTien() != null ? "Tổng tiền: " + formatter.format(dh.getTongTien()) + " VNĐ" : "0 VNĐ";
        holder.tvTongTien.setText(tongTienStr);
        holder.tvTinhTrangGH.setText(dh.getTinhTrangGH() != null ? dh.getTinhTrangGH() : "Chờ giao");

        // 1. Sự kiện Xem Chi Tiết Đơn Hàng -> Mở Activity Chi Tiết Đơn Hàng
        View.OnClickListener detailListener = v -> {
            Intent intent = new Intent(context, com.quanly.app.ui.ChiTietDonHangActivity.class);
            intent.putExtra("donHang", dh);
            intent.putExtra("maDH", dh.getMaDH());
            intent.putExtra("userType", userType);
            context.startActivity(intent);
        };
        holder.itemView.setOnClickListener(detailListener);
        holder.btnViewDetail.setOnClickListener(detailListener);

        // 2. Sự kiện Duyệt Đơn Hàng -> Mở Cửa sổ Xử lý Duyệt đơn
        holder.btnApproveOrder.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.quanly.app.ui.ChiTietDonHangActivity.class);
            intent.putExtra("donHang", dh);
            intent.putExtra("maDH", dh.getMaDH());
            intent.putExtra("userType", userType);
            intent.putExtra("openAction", "APPROVE");
            context.startActivity(intent);
        });

        // 3. Sự kiện Hủy Đơn Hàng -> Mở Cửa sổ Xử lý Hủy đơn
        holder.btnCancelOrder.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.quanly.app.ui.ChiTietDonHangActivity.class);
            intent.putExtra("donHang", dh);
            intent.putExtra("maDH", dh.getMaDH());
            intent.putExtra("userType", userType);
            intent.putExtra("openAction", "CANCEL");
            context.startActivity(intent);
        });

        // 4. Sự kiện NVGH Xác nhận đã giao hàng -> Mở Cửa sổ Xử lý Giao hàng
        holder.btnConfirmDelivery.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.quanly.app.ui.ChiTietDonHangActivity.class);
            intent.putExtra("donHang", dh);
            intent.putExtra("maDH", dh.getMaDH());
            intent.putExtra("userType", userType);
            intent.putExtra("openAction", "DELIVERY");
            context.startActivity(intent);
        });

        // 5. Sự kiện Kế toán Xác nhận thanh toán tiền -> Mở Cửa sổ Xử lý Thu tiền
        holder.btnConfirmPayment.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.quanly.app.ui.ChiTietDonHangActivity.class);
            intent.putExtra("donHang", dh);
            intent.putExtra("maDH", dh.getMaDH());
            intent.putExtra("userType", userType);
            intent.putExtra("openAction", "PAYMENT");
            context.startActivity(intent);
        });
    }

    // ==========================================
    // CỬA SỔ 1: XEM CHI TIẾT ĐƠN HÀNG
    // ==========================================
    public void showOrderDetailDialog(Context context, DonHang dh, int position) {
        Activity activity = getActivityFromContext(context);
        Context dialogCtx = (activity != null) ? activity : context;

        try {
            View dialogView = LayoutInflater.from(dialogCtx).inflate(R.layout.dialog_order_detail, null);

            TextView tvTitle = dialogView.findViewById(R.id.tvOrderTitle);
            ImageView btnDialogClose = dialogView.findViewById(R.id.btnDialogClose);
            TextView tvDetailMaDH = dialogView.findViewById(R.id.tvDetailMaDH);
            TextView tvDetailTrangThai = dialogView.findViewById(R.id.tvDetailTrangThai);
            TextView tvDetailMaDL = dialogView.findViewById(R.id.tvDetailMaDL);
            TextView tvDetailDiemGiao = dialogView.findViewById(R.id.tvDetailDiemGiao);
            TextView tvDetailNgayLap = dialogView.findViewById(R.id.tvDetailNgayLap);
            TextView tvDetailTinhTrangGH = dialogView.findViewById(R.id.tvDetailTinhTrangGH);
            TextView tvDetailThanhToan = dialogView.findViewById(R.id.tvDetailThanhToan);
            TextView tvDetailTongTien = dialogView.findViewById(R.id.tvDetailTongTien);
            TextView tvContent = dialogView.findViewById(R.id.tvOrderDetailContent);
            
            Button btnDialogApproveOrder = dialogView.findViewById(R.id.btnDialogApproveOrder);
            Button btnDialogDeliveryOrder = dialogView.findViewById(R.id.btnDialogDeliveryOrder);
            Button btnDialogPaymentOrder = dialogView.findViewById(R.id.btnDialogPaymentOrder);
            Button btnDialogCancelOrder = dialogView.findViewById(R.id.btnDialogCancelOrder);
            Button btnClose = dialogView.findViewById(R.id.btnCloseDialog);

            String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
            String trangThai = dh.getTrangThai() != null ? dh.getTrangThai() : "Mới tạo";

            if (tvTitle != null) tvTitle.setText("📋 Đơn Hàng #" + maDH);
            if (tvDetailMaDH != null) tvDetailMaDH.setText("Mã ĐH: " + maDH);
            if (tvDetailTrangThai != null) {
                tvDetailTrangThai.setText(trangThai);
                if ("Đã hủy".equalsIgnoreCase(trangThai) || "Từ chối".equalsIgnoreCase(trangThai)) {
                    tvDetailTrangThai.setBackgroundColor(Color.parseColor("#E53935"));
                } else if ("Đã duyệt".equalsIgnoreCase(trangThai) || "Đã xét duyệt".equalsIgnoreCase(trangThai) || "Đã giao".equalsIgnoreCase(trangThai)) {
                    tvDetailTrangThai.setBackgroundColor(Color.parseColor("#4CAF50"));
                } else {
                    tvDetailTrangThai.setBackgroundColor(Color.parseColor("#1E88E5"));
                }
            }
            if (tvDetailMaDL != null) tvDetailMaDL.setText("• Mã khách hàng: " + (dh.getMaDL() != null ? dh.getMaDL() : "DL01"));
            if (tvDetailDiemGiao != null) tvDetailDiemGiao.setText("• Điểm giao: " + (dh.getDiemGiao() != null ? dh.getDiemGiao() : "Chưa cập nhật"));
            if (tvDetailNgayLap != null) tvDetailNgayLap.setText("• Ngày tạo: " + (dh.getNgayLap() != null ? dh.getNgayLap() : "Vừa xong"));
            if (tvDetailTinhTrangGH != null) tvDetailTinhTrangGH.setText("• Giao hàng: " + (dh.getTinhTrangGH() != null ? dh.getTinhTrangGH() : "Chờ giao"));
            if (tvDetailThanhToan != null) {
                String tt = dh.getTinhTrangThanhToan() != null ? dh.getTinhTrangThanhToan() : "Chưa thanh toán";
                tvDetailThanhToan.setText("• " + tt);
                tvDetailThanhToan.setTextColor("Đã thanh toán".equalsIgnoreCase(tt) ? Color.parseColor("#4CAF50") : Color.parseColor("#E53935"));
            }
            if (tvDetailTongTien != null) {
                tvDetailTongTien.setText((dh.getTongTien() != null ? formatter.format(dh.getTongTien()) : "0") + " VNĐ");
            }

            Dialog dialog = buildCustomDialog(dialogCtx, dialogView);

            if (btnDialogClose != null) btnDialogClose.setOnClickListener(v -> dialog.dismiss());
            if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

            boolean isFinalized = "Đã hủy".equalsIgnoreCase(trangThai) || "Từ chối".equalsIgnoreCase(trangThai);
            boolean isApproved = "Đã duyệt".equalsIgnoreCase(trangThai) || "Đã xét duyệt".equalsIgnoreCase(trangThai) || "Đã giao".equalsIgnoreCase(trangThai);
            boolean isDelivered = "Đã giao".equalsIgnoreCase(dh.getTinhTrangGH());
            boolean isPaid = "Đã thanh toán".equalsIgnoreCase(dh.getTinhTrangThanhToan());

            String uType = userType != null && !userType.isEmpty() ? userType : com.quanly.app.utils.SessionManager.getUserType(context);
            boolean isAdmin = uType != null && (uType.contains("QTHT") || uType.equalsIgnoreCase("ADMIN"));
            boolean isKinhDoanh = uType != null && uType.contains("NVKD");
            boolean isKeToan = uType != null && (uType.contains("NVKT") || uType.contains("KE_TOAN"));
            boolean isKho = uType != null && (uType.contains("NVK") || uType.contains("KHO") || uType.contains("THU_KHO"));
            boolean isGiaoHang = uType != null && (uType.contains("NVGH") || uType.contains("GIAO_HANG"));
            boolean isStaff = isAdmin || isKinhDoanh || isKeToan || isKho || isGiaoHang || (uType != null && (uType.contains("STAFF") || uType.contains("NV")));
            boolean isCustomer = !isStaff;

            // Thiết lập quyền hiển thị nút trong Popup theo vai trò
            if (isCustomer) {
                if (btnDialogApproveOrder != null) btnDialogApproveOrder.setVisibility(View.GONE);
                if (btnDialogDeliveryOrder != null) btnDialogDeliveryOrder.setVisibility(View.GONE);
                if (btnDialogPaymentOrder != null) btnDialogPaymentOrder.setVisibility(View.GONE);
                if (btnDialogCancelOrder != null) btnDialogCancelOrder.setVisibility(!isFinalized && !isDelivered ? View.VISIBLE : View.GONE);
            } else if (isKeToan) {
                // Kế toán: Chỉ hiện nút Xác nhận thu tiền
                if (btnDialogApproveOrder != null) btnDialogApproveOrder.setVisibility(View.GONE);
                if (btnDialogDeliveryOrder != null) btnDialogDeliveryOrder.setVisibility(View.GONE);
                if (btnDialogCancelOrder != null) btnDialogCancelOrder.setVisibility(View.GONE);
                if (btnDialogPaymentOrder != null) btnDialogPaymentOrder.setVisibility(!isPaid && !isFinalized ? View.VISIBLE : View.GONE);
            } else if (isGiaoHang) {
                // Giao hàng: Chỉ hiện nút Giao hàng
                if (btnDialogApproveOrder != null) btnDialogApproveOrder.setVisibility(View.GONE);
                if (btnDialogPaymentOrder != null) btnDialogPaymentOrder.setVisibility(View.GONE);
                if (btnDialogCancelOrder != null) btnDialogCancelOrder.setVisibility(View.GONE);
                if (btnDialogDeliveryOrder != null) btnDialogDeliveryOrder.setVisibility(isApproved && !isDelivered && !isFinalized ? View.VISIBLE : View.GONE);
            } else {
                // Admin / Kinh doanh / Kho
                if (btnDialogApproveOrder != null) btnDialogApproveOrder.setVisibility(!isApproved && !isFinalized ? View.VISIBLE : View.GONE);
                if (btnDialogDeliveryOrder != null) btnDialogDeliveryOrder.setVisibility(isApproved && !isDelivered && !isFinalized ? View.VISIBLE : View.GONE);
                if (btnDialogPaymentOrder != null) btnDialogPaymentOrder.setVisibility(!isPaid && !isFinalized ? View.VISIBLE : View.GONE);
                if (btnDialogCancelOrder != null) btnDialogCancelOrder.setVisibility(!isFinalized && !isDelivered ? View.VISIBLE : View.GONE);
            }

            if (btnDialogApproveOrder != null && btnDialogApproveOrder.getVisibility() == View.VISIBLE) {
                btnDialogApproveOrder.setOnClickListener(v -> {
                    dialog.dismiss();
                    showApproveOrderDialog(context, dh, position);
                });
            }

            if (btnDialogDeliveryOrder != null && btnDialogDeliveryOrder.getVisibility() == View.VISIBLE) {
                btnDialogDeliveryOrder.setOnClickListener(v -> {
                    dialog.dismiss();
                    showDeliveryOrderDialog(context, dh, position);
                });
            }

            if (btnDialogPaymentOrder != null && btnDialogPaymentOrder.getVisibility() == View.VISIBLE) {
                btnDialogPaymentOrder.setOnClickListener(v -> {
                    dialog.dismiss();
                    showPaymentOrderDialog(context, dh, position);
                });
            }

            if (btnDialogCancelOrder != null && btnDialogCancelOrder.getVisibility() == View.VISIBLE) {
                btnDialogCancelOrder.setOnClickListener(v -> {
                    dialog.dismiss();
                    showCancelOrderDialog(context, dh, position);
                });
            }

            dialog.show();

            // Tải chi tiết các sản phẩm từ máy chủ
            ApiClient.getApiService().getOrderDetail(maDH).enqueue(new Callback<ApiResponse<java.util.Map<String, Object>>>() {
                @Override
                public void onResponse(Call<ApiResponse<java.util.Map<String, Object>>> call, Response<ApiResponse<java.util.Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        java.util.Map<String, Object> data = response.body().getData();
                        Object chiTietObj = data.get("chiTiet");
                        List<com.quanly.app.model.ChiTietDonHang> ctList = new ArrayList<>();

                        if (chiTietObj instanceof List) {
                            List<?> itemList = (List<?>) chiTietObj;
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            for (Object itemObj : itemList) {
                                try {
                                    String json = gson.toJson(itemObj);
                                    com.quanly.app.model.ChiTietDonHang ct = gson.fromJson(json, com.quanly.app.model.ChiTietDonHang.class);
                                    if (ct != null) {
                                        ctList.add(ct);
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                        if (tvContent != null) {
                            tvContent.setText(buildProductListText(ctList, null));
                        }
                    } else {
                        String msg = (response.body() != null && response.body().getMessage() != null)
                                ? response.body().getMessage() : "Không thể tải chi tiết sản phẩm (Mã lỗi: " + response.code() + ")";
                        if (tvContent != null) {
                            tvContent.setText("⚠️ " + msg);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                    if (tvContent != null) {
                        tvContent.setText("❌ Lỗi kết nối máy chủ: " + t.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Mã ĐH: " + dh.getMaDH() + " | Tổng tiền: " + formatter.format(dh.getTongTien()) + " VNĐ", Toast.LENGTH_LONG).show();
        }
    }

    // ==========================================
    // CỬA SỔ 2: XỬ LÝ HỦY ĐƠN HÀNG
    // ==========================================
    public void showCancelOrderDialog(Context context, DonHang dh, int position) {
        Activity activity = getActivityFromContext(context);
        Context dialogCtx = (activity != null) ? activity : context;

        try {
            View dialogView = LayoutInflater.from(dialogCtx).inflate(R.layout.dialog_cancel_order, null);

            TextView tvCancelMaDH = dialogView.findViewById(R.id.tvCancelMaDH);
            TextView tvCancelTongTien = dialogView.findViewById(R.id.tvCancelTongTien);
            TextView tvCancelDiemGiao = dialogView.findViewById(R.id.tvCancelDiemGiao);
            RadioGroup rgCancelReason = dialogView.findViewById(R.id.rgCancelReason);
            EditText etCancelNote = dialogView.findViewById(R.id.etCancelNote);
            Button btnCancelClose = dialogView.findViewById(R.id.btnCancelClose);
            Button btnConfirmCancelSubmit = dialogView.findViewById(R.id.btnConfirmCancelSubmit);
            ImageView btnCancelDialogClose = dialogView.findViewById(R.id.btnCancelDialogClose);

            String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
            if (tvCancelMaDH != null) tvCancelMaDH.setText("Mã ĐH: " + maDH);
            if (tvCancelTongTien != null) tvCancelTongTien.setText("Tổng giá trị: " + (dh.getTongTien() != null ? formatter.format(dh.getTongTien()) : "0") + " VNĐ");
            if (tvCancelDiemGiao != null) tvCancelDiemGiao.setText("Điểm giao: " + (dh.getDiemGiao() != null ? dh.getDiemGiao() : "Chưa cập nhật"));

            Dialog dialog = buildCustomDialog(dialogCtx, dialogView);

            if (btnCancelDialogClose != null) btnCancelDialogClose.setOnClickListener(v -> dialog.dismiss());
            if (btnCancelClose != null) btnCancelClose.setOnClickListener(v -> dialog.dismiss());

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
                    doCancelOrder(context, dh, position, reason);
                });
            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            doCancelOrder(context, dh, position, "Khách hàng hủy đơn");
        }
    }

    private void doCancelOrder(Context context, DonHang dh, int position, String reason) {
        String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
        Toast.makeText(context, "⏳ Đang tiến hành hủy đơn hàng " + maDH + "...", Toast.LENGTH_SHORT).show();
        ApiClient.getService().updateOrderStatus(maDH, "Đã hủy").enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(context, "✅ Đã hủy thành công đơn hàng " + maDH + "!", Toast.LENGTH_LONG).show();
                    dh.setTrangThai("Đã hủy");
                    notifyItemChanged(position);
                } else {
                    String msg = (response.body() != null && response.body().getMessage() != null) ? response.body().getMessage() : "Không thể hủy đơn hàng!";
                    Toast.makeText(context, "❌ " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                Toast.makeText(context, "❌ Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ==========================================
    // CỬA SỔ 3: XỬ LÝ PHÊ DUYỆT ĐƠN HÀNG
    // ==========================================
    public void showApproveOrderDialog(Context context, DonHang dh, int position) {
        Activity activity = getActivityFromContext(context);
        Context dialogCtx = (activity != null) ? activity : context;

        try {
            View dialogView = LayoutInflater.from(dialogCtx).inflate(R.layout.dialog_approve_order, null);

            TextView tvApproveMaDH = dialogView.findViewById(R.id.tvApproveMaDH);
            TextView tvApproveMaDL = dialogView.findViewById(R.id.tvApproveMaDL);
            TextView tvApproveTongTien = dialogView.findViewById(R.id.tvApproveTongTien);
            TextView tvApproveDiemGiao = dialogView.findViewById(R.id.tvApproveDiemGiao);
            EditText etApproveNote = dialogView.findViewById(R.id.etApproveNote);
            Button btnApproveClose = dialogView.findViewById(R.id.btnApproveClose);
            Button btnConfirmApproveSubmit = dialogView.findViewById(R.id.btnConfirmApproveSubmit);
            ImageView btnApproveDialogClose = dialogView.findViewById(R.id.btnApproveDialogClose);

            String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
            if (tvApproveMaDH != null) tvApproveMaDH.setText("Mã ĐH: " + maDH);
            if (tvApproveMaDL != null) tvApproveMaDL.setText("Khách hàng / Đại lý: " + (dh.getMaDL() != null ? dh.getMaDL() : "DL01"));
            if (tvApproveTongTien != null) tvApproveTongTien.setText("Tổng tiền: " + (dh.getTongTien() != null ? formatter.format(dh.getTongTien()) : "0") + " VNĐ");
            if (tvApproveDiemGiao != null) tvApproveDiemGiao.setText("Điểm giao: " + (dh.getDiemGiao() != null ? dh.getDiemGiao() : "Chưa cập nhật"));

            Dialog dialog = buildCustomDialog(dialogCtx, dialogView);

            if (btnApproveDialogClose != null) btnApproveDialogClose.setOnClickListener(v -> dialog.dismiss());
            if (btnApproveClose != null) btnApproveClose.setOnClickListener(v -> dialog.dismiss());

            if (btnConfirmApproveSubmit != null) {
                btnConfirmApproveSubmit.setOnClickListener(v -> {
                    dialog.dismiss();
                    doApproveOrder(context, dh, position);
                });
            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            doApproveOrder(context, dh, position);
        }
    }

    private void doApproveOrder(Context context, DonHang dh, int position) {
        String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
        Toast.makeText(context, "⏳ Đang phê duyệt và xuất kho đơn hàng " + maDH + "...", Toast.LENGTH_SHORT).show();
        ApiClient.getService().approveOrder(maDH).enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(context, "🎉 Phê duyệt thành công đơn hàng " + maDH + "!", Toast.LENGTH_LONG).show();
                    dh.setTrangThai("Đã duyệt");
                    dh.setTinhTrangGH("Đang giao");
                    notifyItemChanged(position);
                } else {
                    String msg = (response.body() != null && response.body().getMessage() != null) ? response.body().getMessage() : "Không thể phê duyệt đơn hàng!";
                    Toast.makeText(context, "❌ " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                Toast.makeText(context, "❌ Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ==========================================
    // CỬA SỔ 4: XỬ LÝ THU TIỀN / THANH TOÁN
    // ==========================================
    public void showPaymentOrderDialog(Context context, DonHang dh, int position) {
        Activity activity = getActivityFromContext(context);
        Context dialogCtx = (activity != null) ? activity : context;

        try {
            View dialogView = LayoutInflater.from(dialogCtx).inflate(R.layout.dialog_payment_order, null);

            TextView tvPaymentMaDH = dialogView.findViewById(R.id.tvPaymentMaDH);
            TextView tvPaymentMaDL = dialogView.findViewById(R.id.tvPaymentMaDL);
            TextView tvPaymentAmount = dialogView.findViewById(R.id.tvPaymentAmount);
            Button btnPaymentClose = dialogView.findViewById(R.id.btnPaymentClose);
            Button btnConfirmPaymentSubmit = dialogView.findViewById(R.id.btnConfirmPaymentSubmit);
            ImageView btnPaymentDialogClose = dialogView.findViewById(R.id.btnPaymentDialogClose);

            String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
            if (tvPaymentMaDH != null) tvPaymentMaDH.setText("Mã ĐH: " + maDH);
            if (tvPaymentMaDL != null) tvPaymentMaDL.setText("Khách hàng / Đại lý: " + (dh.getMaDL() != null ? dh.getMaDL() : "DL01"));
            if (tvPaymentAmount != null) tvPaymentAmount.setText((dh.getTongTien() != null ? formatter.format(dh.getTongTien()) : "0") + " VNĐ");

            Dialog dialog = buildCustomDialog(dialogCtx, dialogView);

            if (btnPaymentDialogClose != null) btnPaymentDialogClose.setOnClickListener(v -> dialog.dismiss());
            if (btnPaymentClose != null) btnPaymentClose.setOnClickListener(v -> dialog.dismiss());

            if (btnConfirmPaymentSubmit != null) {
                btnConfirmPaymentSubmit.setOnClickListener(v -> {
                    dialog.dismiss();
                    doConfirmPayment(context, dh, position);
                });
            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            doConfirmPayment(context, dh, position);
        }
    }

    private void doConfirmPayment(Context context, DonHang dh, int position) {
        String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
        Toast.makeText(context, "⏳ Đang ghi nhận thu tiền đơn hàng " + maDH + "...", Toast.LENGTH_SHORT).show();
        ApiClient.getApiService().confirmPayment(maDH).enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(context, "💵 Đã xác nhận thu tiền đơn hàng " + maDH + " thành công!", Toast.LENGTH_LONG).show();
                    dh.setTinhTrangThanhToan("Đã thanh toán");
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "❌ Không thể cập nhật thanh toán!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                Toast.makeText(context, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ==========================================
    // CỬA SỔ 5: XỬ LÝ XÁC NHẬN GIAO HÀNG
    // ==========================================
    public void showDeliveryOrderDialog(Context context, DonHang dh, int position) {
        Activity activity = getActivityFromContext(context);
        Context dialogCtx = (activity != null) ? activity : context;

        try {
            View dialogView = LayoutInflater.from(dialogCtx).inflate(R.layout.dialog_delivery_order, null);

            TextView tvDeliveryMaDH = dialogView.findViewById(R.id.tvDeliveryMaDH);
            TextView tvDeliveryMaDL = dialogView.findViewById(R.id.tvDeliveryMaDL);
            TextView tvDeliveryDiemGiao = dialogView.findViewById(R.id.tvDeliveryDiemGiao);
            Button btnDeliveryClose = dialogView.findViewById(R.id.btnDeliveryClose);
            Button btnConfirmDeliverySubmit = dialogView.findViewById(R.id.btnConfirmDeliverySubmit);
            ImageView btnDeliveryDialogClose = dialogView.findViewById(R.id.btnDeliveryDialogClose);

            String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
            if (tvDeliveryMaDH != null) tvDeliveryMaDH.setText("Mã ĐH: " + maDH);
            if (tvDeliveryMaDL != null) tvDeliveryMaDL.setText("Người nhận / Đại lý: " + (dh.getMaDL() != null ? dh.getMaDL() : "DL01"));
            if (tvDeliveryDiemGiao != null) tvDeliveryDiemGiao.setText("📍 Địa chỉ giao: " + (dh.getDiemGiao() != null ? dh.getDiemGiao() : "Chưa cập nhật"));

            Dialog dialog = buildCustomDialog(dialogCtx, dialogView);

            if (btnDeliveryDialogClose != null) btnDeliveryDialogClose.setOnClickListener(v -> dialog.dismiss());
            if (btnDeliveryClose != null) btnDeliveryClose.setOnClickListener(v -> dialog.dismiss());

            if (btnConfirmDeliverySubmit != null) {
                btnConfirmDeliverySubmit.setOnClickListener(v -> {
                    dialog.dismiss();
                    doConfirmDelivery(context, dh, position);
                });
            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            doConfirmDelivery(context, dh, position);
        }
    }

    private void doConfirmDelivery(Context context, DonHang dh, int position) {
        String maDH = dh.getMaDH() != null ? dh.getMaDH().trim() : "";
        Toast.makeText(context, "⏳ Đang cập nhật trạng thái đã giao đơn hàng " + maDH + "...", Toast.LENGTH_SHORT).show();
        ApiClient.getApiService().confirmDelivery(maDH).enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(context, "🎉 Đã xác nhận giao hàng " + maDH + " thành công!", Toast.LENGTH_LONG).show();
                    dh.setTinhTrangGH("Đã giao");
                    dh.setTrangThai("Đã giao");
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "❌ Không thể cập nhật giao hàng!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                Toast.makeText(context, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String buildProductListText(List<com.quanly.app.model.ChiTietDonHang> items, String statusMessage) {
        StringBuilder sb = new StringBuilder();
        if (statusMessage != null) {
            sb.append(statusMessage);
        } else if (items == null || items.isEmpty()) {
            sb.append("(Đơn hàng này chưa có sản phẩm chi tiết)");
        } else {
            int count = 1;
            for (com.quanly.app.model.ChiTietDonHang ct : items) {
                String tenSP = ct.getTenSP() != null && !ct.getTenSP().trim().isEmpty() ? ct.getTenSP() : ("Mã SP: " + ct.getMaSP());
                String donGiaStr = ct.getDonGia() != null ? formatter.format(ct.getDonGia()) + "đ" : "--";
                String thanhTienStr = ct.getThanhTien() != null ? formatter.format(ct.getThanhTien()) + "đ" : "--";

                sb.append(count++).append(". ").append(tenSP)
                  .append("\n    • Số lượng: ").append(ct.getSoLuong() != null ? ct.getSoLuong() : 1)
                  .append("  |  Đơn giá: ").append(donGiaStr)
                  .append("\n    • Thành tiền: ").append(thanhTienStr).append("\n\n");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaDH, tvTrangThai, tvNgayLap, tvDiemGiao, tvTongTien, tvTinhTrangGH;
        Button btnViewDetail, btnApproveOrder, btnCancelOrder, btnConfirmDelivery, btnConfirmPayment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDH = itemView.findViewById(R.id.tvMaDH);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvNgayLap = itemView.findViewById(R.id.tvNgayLap);
            tvDiemGiao = itemView.findViewById(R.id.tvDiemGiao);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);
            tvTinhTrangGH = itemView.findViewById(R.id.tvTinhTrangGH);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnApproveOrder = itemView.findViewById(R.id.btnApproveOrder);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            btnConfirmDelivery = itemView.findViewById(R.id.btnConfirmDelivery);
            btnConfirmPayment = itemView.findViewById(R.id.btnConfirmPayment);
        }
    }
}

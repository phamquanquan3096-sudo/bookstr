package com.quanly.app.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.quanly.app.R;
import com.quanly.app.model.PhieuCongNo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CongNoAdapter extends RecyclerView.Adapter<CongNoAdapter.ViewHolder> {

    public interface OnCongNoActionListener {
        void onThuTien(PhieuCongNo pcn);
        void onChiTiet(PhieuCongNo pcn);
    }

    private List<PhieuCongNo> list = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private OnCongNoActionListener listener;

    public CongNoAdapter(OnCongNoActionListener listener) {
        this.listener = listener;
    }

    public CongNoAdapter() {
        this(null);
    }

    public void setListener(OnCongNoActionListener listener) {
        this.listener = listener;
    }

    public void setList(List<PhieuCongNo> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cong_no, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhieuCongNo pcn = list.get(position);
        Context context = holder.itemView.getContext();

        String maPCN = pcn.getMaPhieuCN() != null ? pcn.getMaPhieuCN() : "--";
        String maDL = pcn.getMaDL() != null ? pcn.getMaDL() : "--";
        double soTienNo = pcn.getSoTienNo() != null ? pcn.getSoTienNo() : 0.0;
        double soTienThu = pcn.getSoTienThu() != null ? pcn.getSoTienThu() : 0.0;
        double conNo = pcn.getConNo() != null ? pcn.getConNo() : Math.max(0.0, soTienNo - soTienThu);

        holder.tvMaPCN.setText("Mã Phiếu: " + maPCN);
        holder.tvMaDL.setText("👤 Khách Hàng: " + maDL);
        holder.tvSoTienNo.setText(formatter.format(soTienNo) + " đ");
        holder.tvSoTienThu.setText(formatter.format(soTienThu) + " đ");
        holder.tvConNo.setText(formatter.format(conNo) + " đ");

        // Ghi chú
        if (pcn.getGhiChu() != null && !pcn.getGhiChu().trim().isEmpty()) {
            holder.tvGhiChu.setVisibility(View.VISIBLE);
            holder.tvGhiChu.setText("📝 Ghi chú: " + pcn.getGhiChu().trim());
        } else {
            holder.tvGhiChu.setVisibility(View.GONE);
        }

        // Tính tiến độ thanh toán
        int calcProgress = 0;
        if (soTienNo > 0) {
            calcProgress = (int) Math.min(100, Math.round((soTienThu / soTienNo) * 100));
        } else if (conNo <= 0) {
            calcProgress = 100;
        }
        final int progress = calcProgress;
        holder.pbThanhToan.setProgress(progress);

        // Trạng thái badge
        if (conNo <= 0) {
            holder.tvTrangThaiCongNo.setText("ĐÃ THU HẾT");
            holder.tvTrangThaiCongNo.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvTrangThaiCongNo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            holder.btnThuTienCongNo.setVisibility(View.GONE);
        } else {
            holder.tvTrangThaiCongNo.setText("CÒN NỢ (" + (100 - progress) + "%)");
            holder.tvTrangThaiCongNo.setTextColor(Color.parseColor("#D32F2F"));
            holder.tvTrangThaiCongNo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
            holder.btnThuTienCongNo.setVisibility(View.VISIBLE);
        }

        // Bấm xem chi tiết -> Mở ChiTietCongNoActivity
        View.OnClickListener detailClick = v -> {
            if (listener != null) {
                listener.onChiTiet(pcn);
            } else {
                android.content.Intent intent = new android.content.Intent(context, com.quanly.app.ui.ChiTietCongNoActivity.class);
                intent.putExtra("phieuCongNo", pcn);
                context.startActivity(intent);
            }
        };
        holder.itemView.setOnClickListener(detailClick);
        holder.btnChiTietCongNo.setOnClickListener(detailClick);

        // Bấm thu tiền -> Mở ChiTietCongNoActivity
        holder.btnThuTienCongNo.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThuTien(pcn);
            } else {
                android.content.Intent intent = new android.content.Intent(context, com.quanly.app.ui.ChiTietCongNoActivity.class);
                intent.putExtra("phieuCongNo", pcn);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaPCN, tvMaDL, tvTrangThaiCongNo, tvSoTienNo, tvSoTienThu, tvConNo, tvGhiChu;
        ProgressBar pbThanhToan;
        Button btnChiTietCongNo, btnThuTienCongNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaPCN = itemView.findViewById(R.id.tvMaPCN);
            tvMaDL = itemView.findViewById(R.id.tvMaDL);
            tvTrangThaiCongNo = itemView.findViewById(R.id.tvTrangThaiCongNo);
            tvSoTienNo = itemView.findViewById(R.id.tvSoTienNo);
            tvSoTienThu = itemView.findViewById(R.id.tvSoTienThu);
            tvConNo = itemView.findViewById(R.id.tvConNo);
            tvGhiChu = itemView.findViewById(R.id.tvGhiChu);
            pbThanhToan = itemView.findViewById(R.id.pbThanhToan);
            btnChiTietCongNo = itemView.findViewById(R.id.btnChiTietCongNo);
            btnThuTienCongNo = itemView.findViewById(R.id.btnThuTienCongNo);
        }
    }
}

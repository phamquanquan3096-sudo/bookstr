package com.quanly.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quanly.app.R;
import com.quanly.app.model.NhanVien;

import java.util.ArrayList;
import java.util.List;

public class NhanVienAdapter extends RecyclerView.Adapter<NhanVienAdapter.NhanVienViewHolder> {

    public interface OnNhanVienActionListener {
        void onEdit(NhanVien nv);
        void onDelete(NhanVien nv);
    }

    private List<NhanVien> list = new ArrayList<>();
    private OnNhanVienActionListener listener;

    public NhanVienAdapter(OnNhanVienActionListener listener) {
        this.listener = listener;
    }

    public void setList(List<NhanVien> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NhanVienViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nhan_vien, parent, false);
        return new NhanVienViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NhanVienViewHolder holder, int position) {
        NhanVien nv = list.get(position);
        holder.tvTenNV.setText(nv.getTenNV());
        holder.tvMaNV.setText("Mã NV: " + nv.getMaNV());
        holder.tvMaChucVu.setText(nv.getMaChucVu() != null ? nv.getMaChucVu() : "N/A");
        holder.tvSDT.setText("SĐT: " + (nv.getSdt() != null ? nv.getSdt() : "---"));
        holder.tvEmail.setText("Email: " + (nv.getEmail() != null ? nv.getEmail() : "---"));

        holder.btnSuaNV.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(nv);
        });

        holder.btnXoaNV.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(nv);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NhanVienViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenNV, tvMaNV, tvMaChucVu, tvSDT, tvEmail;
        Button btnSuaNV, btnXoaNV;

        public NhanVienViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenNV = itemView.findViewById(R.id.tvTenNV);
            tvMaNV = itemView.findViewById(R.id.tvMaNV);
            tvMaChucVu = itemView.findViewById(R.id.tvMaChucVu);
            tvSDT = itemView.findViewById(R.id.tvSDT);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnSuaNV = itemView.findViewById(R.id.btnSuaNV);
            btnXoaNV = itemView.findViewById(R.id.btnXoaNV);
        }
    }
}

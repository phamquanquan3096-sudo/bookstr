package com.quanly.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quanly.app.R;
import com.quanly.app.model.Kho;

import java.util.ArrayList;
import java.util.List;

public class KhoAdapter extends RecyclerView.Adapter<KhoAdapter.ViewHolder> {

    public interface OnKhoClickListener {
        void onChiTiet(Kho kho);
        void onSua(Kho kho);
    }

    private List<Kho> list = new ArrayList<>();
    private final OnKhoClickListener listener;

    public KhoAdapter(OnKhoClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<Kho> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kho, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Kho k = list.get(position);
        holder.tvTenKho.setText(k.getTenKho() != null ? k.getTenKho() : "Kho hàng");
        holder.tvMaKho.setText("Mã: " + (k.getMaKho() != null ? k.getMaKho() : "--"));
        holder.tvDiaChi.setText("📍 Địa chỉ: " + (k.getDiaChi() != null ? k.getDiaChi() : "Chưa cập nhật"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChiTiet(k);
            }
        });

        if (holder.btnChiTietKho != null) {
            holder.btnChiTietKho.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChiTiet(k);
                }
            });
        }

        if (holder.btnEditKho != null) {
            holder.btnEditKho.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSua(k);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenKho, tvMaKho, tvDiaChi;
        Button btnEditKho, btnChiTietKho;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenKho = itemView.findViewById(R.id.tvTenKho);
            tvMaKho = itemView.findViewById(R.id.tvMaKho);
            tvDiaChi = itemView.findViewById(R.id.tvDiaChi);
            btnEditKho = itemView.findViewById(R.id.btnEditKho);
            btnChiTietKho = itemView.findViewById(R.id.btnChiTietKho);
        }
    }
}

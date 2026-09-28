package com.quanly.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.CartItem;
import com.quanly.app.model.SanPham;
import com.quanly.app.utils.CartManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class GioHangAdapter extends RecyclerView.Adapter<GioHangAdapter.ViewHolder> {

    private List<CartItem> list = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public void setList(List<CartItem> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gio_hang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = list.get(position);
        SanPham sp = item.getSanPham();
        if (sp == null) return;

        holder.tvTenSP.setText(sp.getTenSP() != null ? sp.getTenSP() : "Sản phẩm");
        holder.tvMaSP.setText("Mã SP: " + (sp.getMaSP() != null ? sp.getMaSP() : "--"));
        
        String giaStr = sp.getGia() != null ? formatter.format(sp.getGia()) + "đ" : "0đ";
        holder.tvGia.setText(giaStr);
        holder.tvSoLuong.setText(String.valueOf(item.getSoLuong()));

        // Load image using Glide
        String imgUrl = sp.getHinhAnh();
        if (imgUrl != null && !imgUrl.trim().isEmpty()) {
            String fullUrl;
            if (imgUrl.startsWith("http")) {
                fullUrl = imgUrl;
            } else {
                String baseUrl = ApiClient.getBaseUrl();
                if (baseUrl.endsWith("/") && imgUrl.startsWith("/")) {
                    fullUrl = baseUrl + imgUrl.substring(1);
                } else if (!baseUrl.endsWith("/") && !imgUrl.startsWith("/")) {
                    fullUrl = baseUrl + "/" + imgUrl;
                } else {
                    fullUrl = baseUrl + imgUrl;
                }
            }

            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .into(holder.ivHinhAnh);
        } else {
            holder.ivHinhAnh.setImageResource(R.drawable.ic_launcher);
        }

        // Quantity controls
        holder.btnGiam.setOnClickListener(v -> {
            int currentQty = item.getSoLuong();
            if (currentQty > 1) {
                CartManager.getInstance().updateQuantity(sp.getMaSP(), currentQty - 1);
            } else {
                CartManager.getInstance().removeItem(sp.getMaSP());
            }
        });

        holder.btnTang.setOnClickListener(v -> {
            CartManager.getInstance().updateQuantity(sp.getMaSP(), item.getSoLuong() + 1);
        });

        holder.btnDelete.setOnClickListener(v -> {
            CartManager.getInstance().removeItem(sp.getMaSP());
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHinhAnh, btnDelete;
        TextView tvTenSP, tvMaSP, tvGia, tvSoLuong;
        Button btnGiam, btnTang;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHinhAnh = itemView.findViewById(R.id.ivHinhAnh);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvMaSP = itemView.findViewById(R.id.tvMaSP);
            tvGia = itemView.findViewById(R.id.tvGia);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            btnGiam = itemView.findViewById(R.id.btnGiam);
            btnTang = itemView.findViewById(R.id.btnTang);
        }
    }
}

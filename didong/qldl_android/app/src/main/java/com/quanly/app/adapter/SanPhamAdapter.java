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
import com.quanly.app.model.SanPham;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SanPhamAdapter extends RecyclerView.Adapter<SanPhamAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SanPham sp);
    }

    private List<SanPham> list = new ArrayList<>();
    private final OnItemClickListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public SanPhamAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<SanPham> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_san_pham, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SanPham sp = list.get(position);
        holder.tvTenSP.setText(sp.getTenSP() != null ? sp.getTenSP() : "Sản phẩm");
        holder.tvMaSP.setText("Mã: " + (sp.getMaSP() != null ? sp.getMaSP() : "--"));
        
        String giaStr = sp.getGia() != null ? formatter.format(sp.getGia()) + "đ" : "0đ";
        holder.tvGia.setText(giaStr);

        String tonStr = sp.getTongTon() != null ? "Đã bán " + (sp.getTongTon() / 3 + 12) + "+" : "Đã bán 50+";
        holder.tvTonKho.setText(tonStr);

        // Load image using Glide
        String imgUrl = sp.getHinhAnh();
        if (imgUrl != null && !imgUrl.trim().isEmpty()) {
            String firstImg = imgUrl.split(",")[0].trim();
            String fullUrl;
            if (firstImg.startsWith("http://") || firstImg.startsWith("https://") || firstImg.startsWith("content://") || firstImg.startsWith("file://")) {
                fullUrl = firstImg;
            } else {
                String baseUrl = ApiClient.getBaseUrl();
                if (baseUrl.endsWith("/") && firstImg.startsWith("/")) {
                    fullUrl = baseUrl + firstImg.substring(1);
                } else if (!baseUrl.endsWith("/") && !firstImg.startsWith("/")) {
                    fullUrl = baseUrl + "/" + firstImg;
                } else {
                    fullUrl = baseUrl + firstImg;
                }
            }

            if (fullUrl.startsWith("http://") || fullUrl.startsWith("https://")) {
                com.bumptech.glide.load.model.GlideUrl glideUrl = new com.bumptech.glide.load.model.GlideUrl(fullUrl,
                        new com.bumptech.glide.load.model.LazyHeaders.Builder()
                                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                .build());
                Glide.with(holder.itemView.getContext())
                        .load(glideUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.ivHinhAnh);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(fullUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.ivHinhAnh);
            }
        } else {
            holder.ivHinhAnh.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        View.OnClickListener clickListener = v -> {
            if (listener != null) listener.onItemClick(sp);
        };
        holder.itemView.setOnClickListener(clickListener);
        holder.btnChon.setOnClickListener(clickListener);
        if (holder.ivHinhAnh != null) holder.ivHinhAnh.setOnClickListener(clickListener);
        if (holder.tvTenSP != null) holder.tvTenSP.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHinhAnh;
        TextView tvTenSP, tvMaSP, tvGia, tvTonKho;
        Button btnChon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHinhAnh = itemView.findViewById(R.id.ivHinhAnh);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvMaSP = itemView.findViewById(R.id.tvMaSP);
            tvGia = itemView.findViewById(R.id.tvGia);
            tvTonKho = itemView.findViewById(R.id.tvTonKho);
            btnChon = itemView.findViewById(R.id.btnChon);
        }
    }
}

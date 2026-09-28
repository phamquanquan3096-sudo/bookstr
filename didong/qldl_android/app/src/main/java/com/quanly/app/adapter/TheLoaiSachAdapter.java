package com.quanly.app.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quanly.app.R;
import com.quanly.app.model.TheLoaiSach;

import java.util.ArrayList;
import java.util.List;

public class TheLoaiSachAdapter extends RecyclerView.Adapter<TheLoaiSachAdapter.ViewHolder> {

    public interface OnCategoryActionListener {
        void onSelectCategory(TheLoaiSach category);
        void onEditCategory(TheLoaiSach category);
        void onDeleteCategory(TheLoaiSach category);
    }

    private List<TheLoaiSach> fullList = new ArrayList<>();
    private List<TheLoaiSach> displayList = new ArrayList<>();
    private final OnCategoryActionListener listener;
    private boolean isAdmin = false;

    public TheLoaiSachAdapter(OnCategoryActionListener listener, boolean isAdmin) {
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    public void setList(List<TheLoaiSach> list) {
        this.fullList = new ArrayList<>(list);
        this.displayList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            this.displayList = new ArrayList<>(fullList);
        } else {
            String lower = query.trim().toLowerCase();
            List<TheLoaiSach> filtered = new ArrayList<>();
            for (TheLoaiSach item : fullList) {
                if ((item.getTenLoai() != null && item.getTenLoai().toLowerCase().contains(lower)) ||
                    (item.getMaLoai() != null && item.getMaLoai().toLowerCase().contains(lower)) ||
                    (item.getMoTa() != null && item.getMoTa().toLowerCase().contains(lower)) ||
                    (item.getKeywords() != null && item.getKeywords().toLowerCase().contains(lower))) {
                    filtered.add(item);
                }
            }
            this.displayList = filtered;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_the_loai_sach, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TheLoaiSach item = displayList.get(position);

        holder.tvCategoryName.setText(item.getTenLoai() != null ? item.getTenLoai() : "Thể loại sách");
        holder.tvCategoryCode.setText("Mã: " + (item.getMaLoai() != null ? item.getMaLoai() : "--"));
        holder.tvBookCount.setText("• " + item.getSoLuongSach() + "+ đầu sách");
        holder.tvCategoryDesc.setText(item.getMoTa() != null ? item.getMoTa() : "Các tựa sách nổi bật trong danh mục.");
        
        if (item.getKeywords() != null && !item.getKeywords().isEmpty()) {
            holder.tvKeywordTags.setText("Từ khóa: " + item.getKeywords());
            holder.tvKeywordTags.setVisibility(View.VISIBLE);
        } else {
            holder.tvKeywordTags.setVisibility(View.GONE);
        }

        // Setup custom colorful background for icon box
        try {
            int color = Color.parseColor(item.getColorHex());
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.RECTANGLE);
            gd.setCornerRadius(24f);
            gd.setColor(color);
            holder.layoutIconBox.setBackground(gd);
        } catch (Exception e) {
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.RECTANGLE);
            gd.setCornerRadius(24f);
            gd.setColor(Color.parseColor("#1E88E5"));
            holder.layoutIconBox.setBackground(gd);
        }

        // Admin actions
        if (isAdmin) {
            holder.layoutAdminActions.setVisibility(View.VISIBLE);
            holder.btnEditCategory.setOnClickListener(v -> {
                if (listener != null) listener.onEditCategory(item);
            });
            holder.btnDeleteCategory.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteCategory(item);
            });
        } else {
            holder.layoutAdminActions.setVisibility(View.GONE);
        }

        // View books action
        View.OnClickListener selectListener = v -> {
            if (listener != null) listener.onSelectCategory(item);
        };
        holder.itemView.setOnClickListener(selectListener);
        holder.btnViewBooks.setOnClickListener(selectListener);
    }

    @Override
    public int getItemCount() {
        return displayList != null ? displayList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layoutIconBox;
        ImageView ivCategoryIcon, btnEditCategory, btnDeleteCategory;
        TextView tvCategoryName, tvCategoryCode, tvBookCount, tvCategoryDesc, tvKeywordTags;
        LinearLayout layoutAdminActions;
        Button btnViewBooks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutIconBox = itemView.findViewById(R.id.layoutIconBox);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryCode = itemView.findViewById(R.id.tvCategoryCode);
            tvBookCount = itemView.findViewById(R.id.tvBookCount);
            tvCategoryDesc = itemView.findViewById(R.id.tvCategoryDesc);
            tvKeywordTags = itemView.findViewById(R.id.tvKeywordTags);
            layoutAdminActions = itemView.findViewById(R.id.layoutAdminActions);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
            btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
            btnViewBooks = itemView.findViewById(R.id.btnViewBooks);
        }
    }
}

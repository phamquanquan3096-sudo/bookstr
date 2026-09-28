package com.quanly.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.quanly.app.R;
import com.quanly.app.api.ApiClient;

import java.util.ArrayList;
import java.util.List;

public class EditImageThumbAdapter extends RecyclerView.Adapter<EditImageThumbAdapter.ViewHolder> {

    public interface OnImageDeleteListener {
        void onDelete(int position, String url);

        void onSelect(int position, String url);
    }

    private List<String> imageUrls = new ArrayList<>();
    private final OnImageDeleteListener listener;

    public EditImageThumbAdapter(OnImageDeleteListener listener) {
        this.listener = listener;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_image_thumb, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imgUrl = imageUrls.get(position).trim();
        String fullUrl;
        if (imgUrl.startsWith("http://") || imgUrl.startsWith("https://") || imgUrl.startsWith("content://")
                || imgUrl.startsWith("file://")) {
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

        if (fullUrl.startsWith("http://") || fullUrl.startsWith("https://")) {
            GlideUrl glideUrl = new GlideUrl(fullUrl,
                    new LazyHeaders.Builder()
                            .addHeader("User-Agent",
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .build());
            Glide.with(holder.itemView.getContext())
                    .load(glideUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivThumb);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivThumb);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onSelect(position, imgUrl);
        });

        holder.btnDeleteThumb.setOnClickListener(v -> {
            if (listener != null)
                listener.onDelete(position, imgUrl);
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb, btnDeleteThumb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            btnDeleteThumb = itemView.findViewById(R.id.btnDeleteThumb);
        }
    }
}

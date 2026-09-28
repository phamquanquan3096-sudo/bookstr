package com.quanly.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.quanly.app.R;
import com.quanly.app.adapter.EditImageThumbAdapter;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.SanPham;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditSanPhamActivity extends AppCompatActivity {

    private SanPham sanPham;
    private boolean isCreateMode = false;
    private boolean isSyncingText = false;
    private long lastClickTime = 0;
    private ImageView ivPreview, btnBack, btnDeleteTop;
    private TextView tvTitleHeader, tvImageCountBadge;
    private EditText etTenSP, etMaSP, etGia, etHinhAnh, etDonViTinh, etTongTon;
    private Button btnChooseImage, btnSaveProduct, btnDeleteProduct;
    private RecyclerView rvUploadedImages;
    private EditImageThumbAdapter thumbAdapter;
    private final List<String> uploadedImageUrls = new ArrayList<>();

    // Trình chọn nhiều ảnh từ thư viện thiết bị (Tối đa 5 ảnh)
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    // Loại bỏ các URL ảnh mẫu Unsplash cũ để ưu tiên ảnh người dùng tự chọn
                    uploadedImageUrls.removeIf(url -> url.startsWith("https://images.unsplash.com"));

                    int currentCount = uploadedImageUrls.size();
                    int spaceLeft = 5 - currentCount;
                    if (spaceLeft <= 0) {
                        Toast.makeText(this, "⚠️ Đã đạt giới hạn tối đa 5 ảnh cho sản phẩm này!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<Uri> selectedUris = uris;
                    if (selectedUris.size() > spaceLeft) {
                        Toast.makeText(this, "⚠️ Chỉ thêm được tối đa 5 ảnh. Đã tự động giữ lại 5 ảnh đầu!", Toast.LENGTH_LONG).show();
                        selectedUris = selectedUris.subList(0, spaceLeft);
                    }

                    for (Uri uri : selectedUris) {
                        uploadImageToServer(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_edit_san_pham);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sanPham = (SanPham) getIntent().getSerializableExtra("sanPham");
        if (sanPham == null) {
            isCreateMode = true;
            sanPham = new SanPham();
            sanPham.setMaSP("SP" + (1000 + new Random().nextInt(9000)));
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnDeleteTop = findViewById(R.id.btnDeleteTop);
        ivPreview = findViewById(R.id.ivPreview);
        tvTitleHeader = findViewById(R.id.tvTitleHeader);
        tvImageCountBadge = findViewById(R.id.tvImageCountBadge);
        etTenSP = findViewById(R.id.etTenSP);
        etMaSP = findViewById(R.id.etMaSP);
        etGia = findViewById(R.id.etGia);
        etHinhAnh = findViewById(R.id.etHinhAnh);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        rvUploadedImages = findViewById(R.id.rvUploadedImages);
        etDonViTinh = findViewById(R.id.etDonViTinh);
        etTongTon = findViewById(R.id.etTongTon);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);

        if (rvUploadedImages != null) {
            rvUploadedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            thumbAdapter = new EditImageThumbAdapter(new EditImageThumbAdapter.OnImageDeleteListener() {
                @Override
                public void onDelete(int position, String url) {
                    if (position >= 0 && position < uploadedImageUrls.size()) {
                        uploadedImageUrls.remove(position);
                        syncImagesState();
                        Toast.makeText(EditSanPhamActivity.this, "🗑️ Đã xóa 1 ảnh khỏi danh sách!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSelect(int position, String url) {
                    loadPreviewImage(url);
                }
            });
            rvUploadedImages.setAdapter(thumbAdapter);
        }

        if (isCreateMode) {
            if (tvTitleHeader != null) tvTitleHeader.setText("Thêm Sản Phẩm Mới");
            if (btnSaveProduct != null) {
                btnSaveProduct.setText("Lưu Sản Phẩm Mới");
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT
                );
                btnSaveProduct.setLayoutParams(params);
            }
            if (btnDeleteTop != null) btnDeleteTop.setVisibility(android.view.View.GONE);
            if (btnDeleteProduct != null) btnDeleteProduct.setVisibility(android.view.View.GONE);
        } else {
            if (btnDeleteTop != null) btnDeleteTop.setVisibility(android.view.View.VISIBLE);
            if (btnDeleteProduct != null) btnDeleteProduct.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void bindData() {
        etTenSP.setText(sanPham.getTenSP() != null ? sanPham.getTenSP() : "");
        etMaSP.setText(sanPham.getMaSP() != null ? sanPham.getMaSP() : "");
        etGia.setText(sanPham.getGia() != null && sanPham.getGia() > 0 ? String.valueOf(sanPham.getGia()) : "");
        
        String existingImg = sanPham.getHinhAnh() != null ? sanPham.getHinhAnh() : "";
        uploadedImageUrls.clear();
        if (!existingImg.isEmpty()) {
            String[] parts = existingImg.split(",");
            for (String p : parts) {
                if (!p.trim().isEmpty()) {
                    uploadedImageUrls.add(p.trim());
                }
            }
        }
        syncImagesState();

        etDonViTinh.setText(sanPham.getDonViTinh() != null && !sanPham.getDonViTinh().isEmpty() ? sanPham.getDonViTinh() : "Quyển");
        etTongTon.setText(sanPham.getTongTon() != null ? String.valueOf(sanPham.getTongTon()) : "50");
    }

    private void syncImagesState() {
        isSyncingText = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < uploadedImageUrls.size(); i++) {
            sb.append(uploadedImageUrls.get(i));
            if (i < uploadedImageUrls.size() - 1) sb.append(",");
        }
        etHinhAnh.setText(sb.toString());
        isSyncingText = false;
        
        updateImageCountBadge();
        if (thumbAdapter != null) {
            thumbAdapter.setImageUrls(uploadedImageUrls);
        }
        if (!uploadedImageUrls.isEmpty()) {
            loadPreviewImage(uploadedImageUrls.get(0));
        } else {
            loadPreviewImage("");
        }
    }

    private void updateImageCountBadge() {
        if (tvImageCountBadge != null) {
            tvImageCountBadge.setText("Số ảnh: " + uploadedImageUrls.size() + "/5");
        }
    }

    private void uploadImageToServer(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Toast.makeText(this, "⏳ Đang tải ảnh lên Server...", Toast.LENGTH_SHORT).show();

            ApiClient.getService().uploadImage(body).enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String serverUrl = response.body().getData();
                        
                        // Chèn ảnh mới tải lên VÀO ĐẦU DANH SÁCH (Index 0) làm ảnh đại diện chính
                        uploadedImageUrls.add(0, serverUrl);
                        syncImagesState();
                        Toast.makeText(EditSanPhamActivity.this, "🎉 Đã tải ảnh lên Server thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditSanPhamActivity.this, "Không thể tải ảnh lên Server!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Toast.makeText(EditSanPhamActivity.this, "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi đọc tệp ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPreviewImage(String imgUrl) {
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
                GlideUrl glideUrl = new GlideUrl(fullUrl,
                        new LazyHeaders.Builder()
                                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                .build());
                Glide.with(this)
                        .load(glideUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(ivPreview);
            } else {
                Glide.with(this)
                        .load(fullUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(ivPreview);
            }
        } else {
            ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Mở trình chọn ảnh từ máy thiết bị (giới hạn 5 ảnh)
        btnChooseImage.setOnClickListener(v -> {
            if (uploadedImageUrls.size() >= 5) {
                Toast.makeText(this, "⚠️ Đã đạt giới hạn tối đa 5 ảnh cho mỗi sản phẩm!", Toast.LENGTH_SHORT).show();
            } else {
                imagePickerLauncher.launch("image/*");
            }
        });

        etHinhAnh.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isSyncingText) {
                    loadPreviewImage(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSaveProduct.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 800) return;
            lastClickTime = System.currentTimeMillis();
            saveProductData();
        });

        if (btnDeleteTop != null) {
            btnDeleteTop.setOnClickListener(v -> {
                if (System.currentTimeMillis() - lastClickTime < 800) return;
                lastClickTime = System.currentTimeMillis();
                confirmDeleteProduct();
            });
        }

        if (btnDeleteProduct != null) {
            btnDeleteProduct.setOnClickListener(v -> {
                if (System.currentTimeMillis() - lastClickTime < 800) return;
                lastClickTime = System.currentTimeMillis();
                confirmDeleteProduct();
            });
        }
    }

    private void confirmDeleteProduct() {
        String maSP = "";
        if (etMaSP != null && etMaSP.getText() != null && !etMaSP.getText().toString().trim().isEmpty()) {
            maSP = etMaSP.getText().toString().trim();
        } else if (sanPham != null && sanPham.getMaSP() != null) {
            maSP = sanPham.getMaSP().trim();
        }

        String tenSP = "";
        if (etTenSP != null && etTenSP.getText() != null && !etTenSP.getText().toString().trim().isEmpty()) {
            tenSP = etTenSP.getText().toString().trim();
        } else if (sanPham != null && sanPham.getTenSP() != null) {
            tenSP = sanPham.getTenSP().trim();
        }

        android.util.Log.d("EditSanPham", "confirmDeleteProduct called: maSP=" + maSP + ", tenSP=" + tenSP);

        if (maSP.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã sản phẩm để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalMaSP = maSP;
        String finalTenSP = tenSP.isEmpty() ? "Sản phẩm hiện tại" : tenSP;

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvName = dialog.findViewById(R.id.tvDialogProductName);
        TextView tvCode = dialog.findViewById(R.id.tvDialogProductCode);
        Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnDialogConfirm);

        if (tvName != null) tvName.setText(finalTenSP);
        if (tvCode != null) tvCode.setText(finalMaSP);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                executeDeleteProduct(finalMaSP);
            });
        }

        dialog.show();
    }

    private void executeDeleteProduct(String maSP) {
        android.util.Log.d("EditSanPham", "executeDeleteProduct started for: " + maSP);
        if (btnDeleteProduct != null) btnDeleteProduct.setEnabled(false);
        if (btnDeleteTop != null) btnDeleteTop.setEnabled(false);
        if (btnSaveProduct != null) btnSaveProduct.setEnabled(false);

        Toast.makeText(this, "⏳ Đang xóa sản phẩm và dữ liệu liên quan...", Toast.LENGTH_SHORT).show();

        ApiClient.getService().deleteSanPham(maSP).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                android.util.Log.d("EditSanPham", "deleteSanPham onResponse: code=" + response.code() + ", isSuccessful=" + response.isSuccessful());
                if (btnDeleteProduct != null) btnDeleteProduct.setEnabled(true);
                if (btnDeleteTop != null) btnDeleteTop.setEnabled(true);
                if (btnSaveProduct != null) btnSaveProduct.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    android.util.Log.d("EditSanPham", "deleteSanPham SUCCESS! Navigating to MainActivity...");
                    // Dọn dẹp giỏ hàng nếu có chứa sản phẩm vừa xóa
                    com.quanly.app.utils.CartManager.getInstance().removeItem(maSP);

                    Toast.makeText(EditSanPhamActivity.this, "🎉 Đã xóa sản phẩm và tất cả bản ghi liên quan thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Quay về trang chủ (MainActivity)
                    Intent homeIntent = new Intent(EditSanPhamActivity.this, MainActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(homeIntent);
                    finish();
                } else {
                    String err = response.body() != null ? response.body().getMessage() : "Unknown error";
                    android.util.Log.e("EditSanPham", "deleteSanPham failed: " + err);
                    Toast.makeText(EditSanPhamActivity.this, "Không thể xóa sản phẩm: " + err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                android.util.Log.e("EditSanPham", "deleteSanPham onFailure: " + t.getMessage(), t);
                if (btnDeleteProduct != null) btnDeleteProduct.setEnabled(true);
                if (btnDeleteTop != null) btnDeleteTop.setEnabled(true);
                if (btnSaveProduct != null) btnSaveProduct.setEnabled(true);
                Toast.makeText(EditSanPhamActivity.this, "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProductData() {
        String tenSP = etTenSP.getText().toString().trim();
        String giaStr = etGia.getText().toString().trim();
        String hinhAnh = etHinhAnh.getText().toString().trim();
        String donViTinh = etDonViTinh.getText().toString().trim();
        String tongTonStr = etTongTon.getText().toString().trim();

        if (tenSP.isEmpty() || giaStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và giá sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        sanPham.setTenSP(tenSP);
        try {
            sanPham.setGia(Integer.parseInt(giaStr));
        } catch (NumberFormatException e) {
            sanPham.setGia(0);
        }
        sanPham.setHinhAnh(hinhAnh);
        sanPham.setDonViTinh(donViTinh.isEmpty() ? "Quyển" : donViTinh);
        if (!tongTonStr.isEmpty()) {
            try {
                sanPham.setTongTon(Integer.parseInt(tongTonStr));
            } catch (NumberFormatException ignored) {}
        }

        btnSaveProduct.setEnabled(false);
        ApiClient.getService().saveSanPham(sanPham).enqueue(new Callback<ApiResponse<SanPham>>() {
            @Override
            public void onResponse(Call<ApiResponse<SanPham>> call, Response<ApiResponse<SanPham>> response) {
                btnSaveProduct.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String msg = isCreateMode ? "🎉 Thêm sản phẩm mới thành công!" : "🎉 Cập nhật sản phẩm & hình ảnh thành công!";
                    Toast.makeText(EditSanPhamActivity.this, msg, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditSanPhamActivity.this, "Lưu sản phẩm thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SanPham>> call, Throwable t) {
                btnSaveProduct.setEnabled(true);
                Toast.makeText(EditSanPhamActivity.this, "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

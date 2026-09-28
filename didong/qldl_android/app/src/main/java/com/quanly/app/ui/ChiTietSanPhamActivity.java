package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.quanly.app.R;
import com.quanly.app.adapter.ImageSliderAdapter;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.SanPham;
import com.quanly.app.utils.CartManager;
import com.quanly.app.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChiTietSanPhamActivity extends AppCompatActivity implements CartManager.OnCartChangeListener {

    private SanPham sanPham;
    private int quantity = 1;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private long lastClickTime = 0;

    private ImageView btnBack;
    private ViewPager2 vpProductImages;
    private TextView tvTenSP, tvGia, tvMaSP, tvDonViTinh, tvTongTon, tvNgaySX, tvHanSD, tvSoLuong, tvCartBadgeTop, tvImageIndexBadge;
    private Button btnGiam, btnTang, btnAddToCart, btnBuyNow;
    private ImageSliderAdapter sliderAdapter;

    private String userId, userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_chi_tiet_san_pham);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sanPham = (SanPham) getIntent().getSerializableExtra("sanPham");
        userId = getIntent().getStringExtra("userId");
        userType = getIntent().getStringExtra("userType");

        if (userId == null || userId.isEmpty()) {
            userId = SessionManager.getUserId(this);
        }
        if (userType == null || userType.isEmpty()) {
            userType = SessionManager.getUserType(this);
        }
        if (userType == null || userType.isEmpty()) {
            userType = "ADMIN";
        }

        if (sanPham == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindData();
        setupListeners();

        CartManager.getInstance().addListener(this);
        updateCartBadge(CartManager.getInstance().getTotalCount());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("isDeleted", false)) {
                setResult(RESULT_OK);
                finish();
                return;
            }
            // Khi sửa sản phẩm xong quay lại thì báo RESULT_OK để làm mới giao diện
            setResult(RESULT_OK);
            fetchLatestProductData();
        }
    }

    private void fetchLatestProductData() {
        if (sanPham == null || sanPham.getMaSP() == null) return;
        ApiClient.getService().getSanPhams(sanPham.getMaSP()).enqueue(new retrofit2.Callback<ApiResponse<List<SanPham>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<SanPham>>> call, retrofit2.Response<ApiResponse<List<SanPham>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    for (SanPham sp : response.body().getData()) {
                        if (sanPham.getMaSP().equalsIgnoreCase(sp.getMaSP())) {
                            sanPham = sp;
                            bindData();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<SanPham>>> call, Throwable t) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartManager.getInstance().removeListener(this);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        vpProductImages = findViewById(R.id.vpProductImages);
        tvImageIndexBadge = findViewById(R.id.tvImageIndexBadge);
        tvTenSP = findViewById(R.id.tvTenSP);
        tvGia = findViewById(R.id.tvGia);
        tvMaSP = findViewById(R.id.tvMaSP);
        tvDonViTinh = findViewById(R.id.tvDonViTinh);
        tvTongTon = findViewById(R.id.tvTongTon);
        tvNgaySX = findViewById(R.id.tvNgaySX);
        tvHanSD = findViewById(R.id.tvHanSD);
        tvSoLuong = findViewById(R.id.tvSoLuong);
        tvCartBadgeTop = findViewById(R.id.tvCartBadgeTop);

        btnGiam = findViewById(R.id.btnGiam);
        btnTang = findViewById(R.id.btnTang);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        sliderAdapter = new ImageSliderAdapter();
        if (vpProductImages != null) {
            vpProductImages.setAdapter(sliderAdapter);
        }

        boolean canEditProduct = userType != null && (userType.contains("QTHT") || userType.contains("NVKD") || userType.equalsIgnoreCase("ADMIN"));
        ImageView btnEditProduct = findViewById(R.id.btnEditProduct);
        if (btnEditProduct != null) {
            if (canEditProduct) {
                btnEditProduct.setVisibility(android.view.View.VISIBLE);
                btnEditProduct.setOnClickListener(v -> {
                    if (System.currentTimeMillis() - lastClickTime < 800) return;
                    lastClickTime = System.currentTimeMillis();
                    Intent intent = new Intent(ChiTietSanPhamActivity.this, EditSanPhamActivity.class);
                    intent.putExtra("sanPham", sanPham);
                    startActivityForResult(intent, 101);
                });
            } else {
                btnEditProduct.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void bindData() {
        tvTenSP.setText(sanPham.getTenSP() != null ? sanPham.getTenSP() : "Sản phẩm");
        tvMaSP.setText("Mã SP: " + (sanPham.getMaSP() != null ? sanPham.getMaSP() : "--"));
        
        String giaStr = sanPham.getGia() != null ? formatter.format(sanPham.getGia()) + "đ" : "0đ";
        tvGia.setText(giaStr);

        String dvt = sanPham.getDonViTinh();
        if (dvt == null || dvt.trim().isEmpty()) {
            dvt = "Quyển";
        }
        tvDonViTinh.setText("Đơn vị: " + dvt);
        tvTongTon.setText("Tồn kho: " + (sanPham.getTongTon() != null ? sanPham.getTongTon() : 0));

        if (sanPham.getNgaySX() != null && !sanPham.getNgaySX().isEmpty()) {
            tvNgaySX.setText("Ngày sản xuất: " + sanPham.getNgaySX());
        } else {
            tvNgaySX.setText("Ngày sản xuất: 2025-02-28");
        }

        if (sanPham.getHanSD() != null && !sanPham.getHanSD().isEmpty()) {
            tvHanSD.setText("Hạn sử dụng: " + sanPham.getHanSD());
        } else {
            tvHanSD.setText("Hạn sử dụng: 2030-08-23");
        }

        String imgUrl = sanPham.getHinhAnh();
        List<String> imageList = new ArrayList<>();
        if (imgUrl != null && !imgUrl.trim().isEmpty()) {
            String[] parts = imgUrl.split(",");
            for (String p : parts) {
                if (!p.trim().isEmpty()) {
                    imageList.add(p.trim());
                }
            }
        }
        if (imageList.isEmpty()) {
            imageList.add("");
        }

        sliderAdapter.setImageUrls(imageList);

        if (tvImageIndexBadge != null) {
            tvImageIndexBadge.setText("1/" + imageList.size());
        }

        if (vpProductImages != null) {
            vpProductImages.unregisterOnPageChangeCallback(pageChangeCallback);
            vpProductImages.registerOnPageChangeCallback(pageChangeCallback);
        }
    }

    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            if (tvImageIndexBadge != null && sliderAdapter != null) {
                tvImageIndexBadge.setText((position + 1) + "/" + sliderAdapter.getItemCount());
            }
        }
    };

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnCartTop).setOnClickListener(v -> {
            Intent intent = new Intent(this, GioHangActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnGiam.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvSoLuong.setText(String.valueOf(quantity));
            }
        });

        btnTang.setOnClickListener(v -> {
            quantity++;
            tvSoLuong.setText(String.valueOf(quantity));
        });

        btnAddToCart.setOnClickListener(v -> {
            CartManager.getInstance().addToCart(sanPham, quantity);
            Toast.makeText(this, "Đã thêm " + quantity + " " + sanPham.getTenSP() + " vào giỏ hàng!", Toast.LENGTH_SHORT).show();
        });

        btnBuyNow.setOnClickListener(v -> {
            CartManager.getInstance().addToCart(sanPham, quantity);
            Intent intent = new Intent(this, GioHangActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    private void updateCartBadge(int totalCount) {
        if (tvCartBadgeTop != null) {
            tvCartBadgeTop.setText(String.valueOf(totalCount));
        }
    }

    @Override
    public void onCartChanged(int totalCount, double totalAmount) {
        updateCartBadge(totalCount);
    }
}

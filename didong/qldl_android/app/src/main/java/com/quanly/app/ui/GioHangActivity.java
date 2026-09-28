package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quanly.app.R;
import com.quanly.app.adapter.GioHangAdapter;
import com.quanly.app.model.CartItem;
import com.quanly.app.utils.CartManager;
import com.quanly.app.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.List;

public class GioHangActivity extends AppCompatActivity implements CartManager.OnCartChangeListener {

    private ImageView btnBack;
    private TextView tvEmpty, tvTongTien, btnClearCart;
    private RecyclerView rvGioHang;
    private Button btnCheckout;
    private GioHangAdapter adapter;
    private String userId;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_gio_hang);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.trim().isEmpty()) {
            userId = SessionManager.getUserId(this);
        }

        initViews();
        setupRecyclerView();
        setupListeners();

        CartManager.getInstance().addListener(this);
        loadCartData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartManager.getInstance().removeListener(this);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnClearCart = findViewById(R.id.btnClearCart);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTongTien = findViewById(R.id.tvTongTien);
        rvGioHang = findViewById(R.id.rvGioHang);
        btnCheckout = findViewById(R.id.btnCheckout);
    }

    private void setupRecyclerView() {
        adapter = new GioHangAdapter();
        rvGioHang.setLayoutManager(new LinearLayoutManager(this));
        rvGioHang.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearCart.setOnClickListener(v -> {
            if (CartManager.getInstance().getTotalCount() > 0) {
                CartManager.getInstance().clearCart();
                Toast.makeText(this, "Đã xóa toàn bộ giỏ hàng!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý luồng TIẾN HÀNH ĐẶT HÀNG
        btnCheckout.setOnClickListener(v -> {
            if (CartManager.getInstance().getTotalCount() == 0) {
                Toast.makeText(this, "Giỏ hàng đang trống! Vui lòng chọn sản phẩm trước.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId == null || userId.trim().isEmpty()) {
                // Chưa đăng nhập ➔ Chuyển tới cửa sổ Đăng nhập
                Toast.makeText(this, "Vui lòng đăng nhập để tiến hành đặt hàng!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GioHangActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                // Đã đăng nhập ➔ Chuyển sang Cửa sổ Thanh toán
                Intent intent = new Intent(GioHangActivity.this, ThanhToanActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }

    private void loadCartData() {
        List<CartItem> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) {
            rvGioHang.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvGioHang.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            adapter.setList(items);
        }
        updateTotalAmount(CartManager.getInstance().getTotalAmount());
    }

    private void updateTotalAmount(double amount) {
        String amountStr = amount > 0 ? formatter.format(amount) + "đ" : "0đ";
        tvTongTien.setText(amountStr);
    }

    @Override
    public void onCartChanged(int totalCount, double totalAmount) {
        loadCartData();
    }
}

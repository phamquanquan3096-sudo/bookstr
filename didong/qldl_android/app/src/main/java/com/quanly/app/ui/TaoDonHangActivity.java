package com.quanly.app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quanly.app.R;
import com.quanly.app.adapter.SanPhamAdapter;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.DonHang;
import com.quanly.app.model.OrderCreateRequest;
import com.quanly.app.model.OrderItemDto;
import com.quanly.app.model.SanPham;
import com.quanly.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaoDonHangActivity extends AppCompatActivity {

    private EditText etDiemGiao;
    private RecyclerView rvChonSanPham;
    private Button btnXacNhanTao;

    private SanPhamAdapter adapter;
    private final List<OrderItemDto> selectedItems = new ArrayList<>();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_don_hang);

        userId = getIntent().getStringExtra("userId");
        if ((userId == null || userId.isEmpty()) && SessionManager.isLoggedIn(this)) {
            userId = SessionManager.getUserId(this);
        }
        if (userId == null || userId.isEmpty()) userId = "DL01";

        etDiemGiao = findViewById(R.id.etDiemGiao);
        rvChonSanPham = findViewById(R.id.rvChonSanPham);
        btnXacNhanTao = findViewById(R.id.btnXacNhanTao);

        rvChonSanPham.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SanPhamAdapter(sp -> {
            selectedItems.add(new OrderItemDto(sp.getMaSP(), 1));
            Toast.makeText(this, "Đã thêm 1 " + sp.getTenSP() + " vào đơn hàng", Toast.LENGTH_SHORT).show();
        });
        rvChonSanPham.setAdapter(adapter);

        btnXacNhanTao.setOnClickListener(v -> submitOrder());

        loadSanPhams();
    }

    private void loadSanPhams() {
        ApiClient.getService().getSanPhams(null).enqueue(new Callback<ApiResponse<List<SanPham>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SanPham>>> call, Response<ApiResponse<List<SanPham>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    adapter.setList(response.body().getData());
                }
            }
            @Override public void onFailure(Call<ApiResponse<List<SanPham>>> call, Throwable t) {}
        });
    }

    private void submitOrder() {
        String diemGiao = etDiemGiao.getText().toString().trim();
        if (diemGiao.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        OrderCreateRequest request = new OrderCreateRequest(userId, diemGiao, selectedItems);
        ApiClient.getService().createDonHang(request).enqueue(new Callback<ApiResponse<DonHang>>() {
            @Override
            public void onResponse(Call<ApiResponse<DonHang>> call, Response<ApiResponse<DonHang>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(TaoDonHangActivity.this, "Tạo đơn hàng thành công: " + response.body().getData().getMaDH(), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi tạo đơn hàng";
                    Toast.makeText(TaoDonHangActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DonHang>> call, Throwable t) {
                Toast.makeText(TaoDonHangActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.Kho;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietKhoActivity extends AppCompatActivity {

    private ImageView btnBackHeader;
    private Button btnEditKhoHeader, btnEditKhoAction, btnDeleteKhoAction;
    private TextView tvTenKhoDetail, tvMaKhoDetail, tvDiaChiDetail, tvTrangThaiKho;

    private Kho currentKho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_kho);

        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            Object extra = getIntent().getSerializableExtra("kho");
            if (extra instanceof Kho) {
                currentKho = (Kho) extra;
            }

            if (currentKho == null) {
                String maKho = getIntent().getStringExtra("maKho");
                if (maKho != null && !maKho.isEmpty()) {
                    currentKho = new Kho(maKho, getIntent().getStringExtra("tenKho"), getIntent().getStringExtra("diaChi"));
                }
            }

            if (currentKho == null) {
                Toast.makeText(this, "Không tìm thấy thông tin kho hàng!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            initViews();
            bindData();
            setupListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        btnBackHeader = findViewById(R.id.btnBackHeader);
        btnEditKhoHeader = findViewById(R.id.btnEditKhoHeader);
        btnEditKhoAction = findViewById(R.id.btnEditKhoAction);
        btnDeleteKhoAction = findViewById(R.id.btnDeleteKhoAction);
        tvTenKhoDetail = findViewById(R.id.tvTenKhoDetail);
        tvMaKhoDetail = findViewById(R.id.tvMaKhoDetail);
        tvDiaChiDetail = findViewById(R.id.tvDiaChiDetail);
        tvTrangThaiKho = findViewById(R.id.tvTrangThaiKho);
    }

    private void bindData() {
        if (currentKho == null) return;

        tvTenKhoDetail.setText(currentKho.getTenKho() != null ? currentKho.getTenKho() : "Kho Hàng");
        tvMaKhoDetail.setText("Mã kho: " + (currentKho.getMaKho() != null ? currentKho.getMaKho() : "--"));
        tvDiaChiDetail.setText(currentKho.getDiaChi() != null ? "📍 " + currentKho.getDiaChi() : "📍 Chưa cập nhật địa chỉ");
    }

    private void setupListeners() {
        btnBackHeader.setOnClickListener(v -> finish());

        View.OnClickListener editClick = v -> {
            Intent intent = new Intent(ChiTietKhoActivity.this, EditKhoActivity.class);
            intent.putExtra("kho", currentKho);
            startActivityForResult(intent, 301);
        };

        btnEditKhoHeader.setOnClickListener(editClick);
        btnEditKhoAction.setOnClickListener(editClick);

        btnDeleteKhoAction.setOnClickListener(v -> confirmDeleteKho());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 301 && resultCode == RESULT_OK) {
            // Tải lại thông tin kho sau khi sửa
            if (currentKho != null && currentKho.getMaKho() != null) {
                ApiClient.getService().getKhoByMa(currentKho.getMaKho()).enqueue(new Callback<ApiResponse<Kho>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Kho>> call, Response<ApiResponse<Kho>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentKho = response.body().getData();
                            bindData();
                            setResult(RESULT_OK);
                        }
                    }
                    @Override public void onFailure(Call<ApiResponse<Kho>> call, Throwable t) {}
                });
            }
        }
    }

    private void confirmDeleteKho() {
        if (currentKho == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa kho hàng")
                .setMessage("Bạn có chắc chắn muốn xóa kho \"" + currentKho.getTenKho() + "\" (" + currentKho.getMaKho() + ")?")
                .setPositiveButton("Xóa ngay", (dialog, which) -> {
                    ApiClient.getService().deleteKho(currentKho.getMaKho()).enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(ChiTietKhoActivity.this, "Đã xóa kho hàng thành công!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(ChiTietKhoActivity.this, "Không thể xóa kho!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            Toast.makeText(ChiTietKhoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

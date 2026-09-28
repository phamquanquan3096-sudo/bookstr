package com.quanly.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.Kho;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditKhoActivity extends AppCompatActivity {

    private ImageView btnBackHeader;
    private TextView tvHeaderTitle;
    private Button btnDeleteKhoHeader;
    private TextInputLayout tilMaKho, tilTenKho, tilDiaChi;
    private EditText etMaKho, etTenKho, etDiaChi;
    private Button btnSaveKho;

    private Kho currentKho;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_kho);

        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            Object extra = getIntent().getSerializableExtra("kho");
            if (extra instanceof Kho) {
                currentKho = (Kho) extra;
            }

            if (currentKho != null && currentKho.getMaKho() != null && !currentKho.getMaKho().isEmpty()) {
                isEditMode = true;
            }

            initViews();
            bindData();
            setupListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo màn hình: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        btnBackHeader = findViewById(R.id.btnBackHeader);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        btnDeleteKhoHeader = findViewById(R.id.btnDeleteKhoHeader);
        tilMaKho = findViewById(R.id.tilMaKho);
        tilTenKho = findViewById(R.id.tilTenKho);
        tilDiaChi = findViewById(R.id.tilDiaChi);
        etMaKho = findViewById(R.id.etMaKho);
        etTenKho = findViewById(R.id.etTenKho);
        etDiaChi = findViewById(R.id.etDiaChi);
        btnSaveKho = findViewById(R.id.btnSaveKho);
    }

    private void bindData() {
        if (isEditMode) {
            tvHeaderTitle.setText("CHỈNH SỬA KHO HÀNG");
            btnDeleteKhoHeader.setVisibility(View.VISIBLE);
            etMaKho.setText(currentKho.getMaKho());
            etMaKho.setEnabled(false);
            etTenKho.setText(currentKho.getTenKho() != null ? currentKho.getTenKho() : "");
            etDiaChi.setText(currentKho.getDiaChi() != null ? currentKho.getDiaChi() : "");
            btnSaveKho.setText("💾 CẬP NHẬT KHO HÀNG");
        } else {
            tvHeaderTitle.setText("THÊM KHO HÀNG MỚI");
            btnDeleteKhoHeader.setVisibility(View.GONE);
            String autoMaKho = "KO" + (100 + new Random().nextInt(900));
            etMaKho.setText(autoMaKho);
            btnSaveKho.setText("➕ THÊM KHO HÀNG MỚI");
        }
    }

    private void setupListeners() {
        btnBackHeader.setOnClickListener(v -> finish());

        btnDeleteKhoHeader.setOnClickListener(v -> confirmDeleteKho());

        btnSaveKho.setOnClickListener(v -> saveKho());
    }

    private void saveKho() {
        String maKho = etMaKho.getText().toString().trim();
        String tenKho = etTenKho.getText().toString().trim();
        String diaChi = etDiaChi.getText().toString().trim();

        if (tenKho.isEmpty()) {
            tilTenKho.setError("Vui lòng nhập tên kho hàng!");
            etTenKho.requestFocus();
            return;
        } else {
            tilTenKho.setError(null);
        }

        btnSaveKho.setEnabled(false);
        btnSaveKho.setText("Đang lưu...");

        Kho khoData = new Kho();
        khoData.setMaKho(maKho);
        khoData.setTenKho(tenKho);
        khoData.setDiaChi(diaChi.isEmpty() ? "Chưa cập nhật địa chỉ" : diaChi);

        if (isEditMode) {
            ApiClient.getService().updateKho(maKho, khoData).enqueue(new Callback<ApiResponse<Kho>>() {
                @Override
                public void onResponse(Call<ApiResponse<Kho>> call, Response<ApiResponse<Kho>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(EditKhoActivity.this, "🎉 Đã cập nhật kho hàng thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        btnSaveKho.setEnabled(true);
                        btnSaveKho.setText("💾 CẬP NHẬT KHO HÀNG");
                        String msg = response.body() != null ? response.body().getMessage() : "Cập nhật thất bại";
                        Toast.makeText(EditKhoActivity.this, "❌ " + msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Kho>> call, Throwable t) {
                    btnSaveKho.setEnabled(true);
                    btnSaveKho.setText("💾 CẬP NHẬT KHO HÀNG");
                    Toast.makeText(EditKhoActivity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ApiClient.getService().createKho(khoData).enqueue(new Callback<ApiResponse<Kho>>() {
                @Override
                public void onResponse(Call<ApiResponse<Kho>> call, Response<ApiResponse<Kho>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(EditKhoActivity.this, "🎉 Đã thêm kho hàng mới thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        btnSaveKho.setEnabled(true);
                        btnSaveKho.setText("➕ THÊM KHO HÀNG MỚI");
                        String msg = response.body() != null ? response.body().getMessage() : "Thêm kho thất bại";
                        Toast.makeText(EditKhoActivity.this, "❌ " + msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Kho>> call, Throwable t) {
                    btnSaveKho.setEnabled(true);
                    btnSaveKho.setText("➕ THÊM KHO HÀNG MỚI");
                    Toast.makeText(EditKhoActivity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void confirmDeleteKho() {
        if (currentKho == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa kho")
                .setMessage("Bạn có chắc chắn muốn xóa kho \"" + currentKho.getTenKho() + "\" (" + currentKho.getMaKho() + ")?")
                .setPositiveButton("Xóa kho ngay", (dialog, which) -> {
                    ApiClient.getService().deleteKho(currentKho.getMaKho()).enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(EditKhoActivity.this, "Đã xóa kho thành công!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(EditKhoActivity.this, "Không thể xóa kho!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            Toast.makeText(EditKhoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

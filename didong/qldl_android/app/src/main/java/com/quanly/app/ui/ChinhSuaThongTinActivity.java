package com.quanly.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.DaiLy;
import com.quanly.app.model.NhanVien;
import com.quanly.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChinhSuaThongTinActivity extends AppCompatActivity {

    private String userId, fullName, userType;
    private EditText etFullName, etPhone, etEmail, etAddress, etPassword;
    private LinearLayout layoutAddress, layoutPassword;
    private Button btnSave;
    private ImageView btnBack;
    private TextView tvHeaderTitle;

    private boolean isStaff;
    private NhanVien currentNhanVien;
    private DaiLy currentDaiLy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinh_sua_thong_tin);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userId = getIntent().getStringExtra("userId");
        fullName = getIntent().getStringExtra("fullName");
        userType = getIntent().getStringExtra("userType");

        if ((userId == null || userId.isEmpty()) && SessionManager.isLoggedIn(this)) {
            userId = SessionManager.getUserId(this);
            fullName = SessionManager.getFullName(this);
            userType = SessionManager.getUserType(this);
        }

        isStaff = userType != null && (userType.contains("QTHT") || userType.contains("NVKD")
                || userType.contains("NVKT") || userType.contains("NVK") || userType.contains("KHO")
                || userType.contains("NVGH") || userType.contains("GIAO_HANG")
                || userType.equalsIgnoreCase("ADMIN") || userType.contains("STAFF"));

        btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        layoutAddress = findViewById(R.id.layoutAddress);
        layoutPassword = findViewById(R.id.layoutPassword);
        btnSave = findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> finish());

        if (isStaff) {
            tvHeaderTitle.setText("Chỉnh sửa thông tin Nhân viên");
            layoutAddress.setVisibility(View.GONE);
            layoutPassword.setVisibility(View.VISIBLE);
        } else {
            tvHeaderTitle.setText("Chỉnh sửa thông tin Khách hàng");
            layoutAddress.setVisibility(View.VISIBLE);
            layoutPassword.setVisibility(View.GONE);
        }

        if (fullName != null) etFullName.setText(fullName);

        loadInitialData();

        btnSave.setOnClickListener(v -> handleSave());
    }

    private void loadInitialData() {
        if (userId == null || userId.isEmpty()) return;

        if (isStaff) {
            ApiClient.getApiService().getNhanVienByMa(userId).enqueue(new Callback<ApiResponse<NhanVien>>() {
                @Override
                public void onResponse(Call<ApiResponse<NhanVien>> call, Response<ApiResponse<NhanVien>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        currentNhanVien = response.body().getData();
                        if (currentNhanVien.getTenNV() != null && !currentNhanVien.getTenNV().isEmpty()) {
                            etFullName.setText(currentNhanVien.getTenNV());
                        }
                        if (currentNhanVien.getSdt() != null) etPhone.setText(currentNhanVien.getSdt());
                        if (currentNhanVien.getEmail() != null) etEmail.setText(currentNhanVien.getEmail());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<NhanVien>> call, Throwable t) {
                }
            });
        } else {
            ApiClient.getApiService().getDaiLyDetail(userId).enqueue(new Callback<ApiResponse<DaiLy>>() {
                @Override
                public void onResponse(Call<ApiResponse<DaiLy>> call, Response<ApiResponse<DaiLy>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        currentDaiLy = response.body().getData();
                        if (currentDaiLy.getTenDL() != null && !currentDaiLy.getTenDL().isEmpty()) {
                            etFullName.setText(currentDaiLy.getTenDL());
                        }
                        if (currentDaiLy.getSdt() != null) etPhone.setText(currentDaiLy.getSdt());
                        if (currentDaiLy.getEmail() != null) etEmail.setText(currentDaiLy.getEmail());
                        if (currentDaiLy.getDiaChi() != null) etAddress.setText(currentDaiLy.getDiaChi());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<DaiLy>> call, Throwable t) {
                }
            });
        }
    }

    private void handleSave() {
        String newName = etFullName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên và số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        if (isStaff) {
            String newPassword = etPassword.getText().toString().trim();
            if (currentNhanVien == null) {
                currentNhanVien = new NhanVien();
                currentNhanVien.setMaNV(userId != null ? userId : "4");
                currentNhanVien.setMaChucVu(userType != null ? userType : "QTHT");
            }
            currentNhanVien.setTenNV(newName);
            currentNhanVien.setSdt(newPhone);
            currentNhanVien.setEmail(newEmail);
            if (!newPassword.isEmpty()) {
                currentNhanVien.setPassword(newPassword);
            }

            ApiClient.getApiService().updateNhanVien(userId != null ? userId : currentNhanVien.getMaNV(), currentNhanVien)
                    .enqueue(new Callback<ApiResponse<NhanVien>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<NhanVien>> call, Response<ApiResponse<NhanVien>> response) {
                            btnSave.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(ChinhSuaThongTinActivity.this, "🎉 Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                                SessionManager.saveSession(ChinhSuaThongTinActivity.this, userId, newName, userType);
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                String msg = response.body() != null ? response.body().getMessage() : "Cập nhật thất bại!";
                                Toast.makeText(ChinhSuaThongTinActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<NhanVien>> call, Throwable t) {
                            btnSave.setEnabled(true);
                            Toast.makeText(ChinhSuaThongTinActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            String newAddress = etAddress.getText().toString().trim();
            if (currentDaiLy == null) {
                currentDaiLy = new DaiLy();
                currentDaiLy.setMaDL(userId != null ? userId : "DL01");
            }
            currentDaiLy.setTenDL(newName);
            currentDaiLy.setSdt(newPhone);
            currentDaiLy.setDiaChi(newAddress);
            currentDaiLy.setEmail(newEmail);

            ApiClient.getApiService().updateDaiLy(currentDaiLy)
                    .enqueue(new Callback<ApiResponse<DaiLy>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<DaiLy>> call, Response<ApiResponse<DaiLy>> response) {
                            btnSave.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(ChinhSuaThongTinActivity.this, "🎉 Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                                SessionManager.saveSession(ChinhSuaThongTinActivity.this, userId, newName, userType);
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                String msg = response.body() != null ? response.body().getMessage() : "Cập nhật thất bại!";
                                Toast.makeText(ChinhSuaThongTinActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<DaiLy>> call, Throwable t) {
                            btnSave.setEnabled(true);
                            Toast.makeText(ChinhSuaThongTinActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

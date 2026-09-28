package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.DaiLy;
import com.quanly.app.model.LoginRequest;
import com.quanly.app.model.LoginResponse;
import com.quanly.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangKyActivity extends AppCompatActivity {

    private EditText etRegFullName, etRegPhone, etRegUsername, etRegPassword;
    private Button btnSubmitRegister;
    private View btnBackToLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_dang_ky);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        etRegFullName = findViewById(R.id.etRegFullName);
        etRegPhone = findViewById(R.id.etRegPhone);
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);
        progressBar = findViewById(R.id.progressBar);

        btnBackToLogin.setOnClickListener(v -> finish());

        btnSubmitRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String fullName = etRegFullName.getText().toString().trim();
        String phone = etRegPhone.getText().toString().trim();
        String username = etRegUsername.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên, tên đăng nhập và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmitRegister.setEnabled(false);

        DaiLy daiLy = new DaiLy(username, password, fullName, phone, "Việt Nam", phone);

        ApiClient.getService().registerDaily(daiLy).enqueue(new Callback<ApiResponse<DaiLy>>() {
            @Override
            public void onResponse(Call<ApiResponse<DaiLy>> call, Response<ApiResponse<DaiLy>> response) {
                progressBar.setVisibility(View.GONE);
                btnSubmitRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DangKyActivity.this, "🎉 Đăng ký thành công! Đang tự động đăng nhập...",
                            Toast.LENGTH_LONG).show();

                    // Tự động đăng nhập sau khi đăng ký thành công
                    LoginRequest loginReq = new LoginRequest(username, password, "ANY");
                    ApiClient.getService().login(loginReq).enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                            if (res.isSuccessful() && res.body() != null && res.body().isSuccess()) {
                                LoginResponse r = res.body();
                                SessionManager.saveSession(DangKyActivity.this, r.getUserId(), r.getFullName(),
                                        r.getUserType());

                                Intent intent = new Intent(DangKyActivity.this, MainActivity.class);
                                intent.putExtra("userId", r.getUserId());
                                intent.putExtra("fullName", r.getFullName());
                                intent.putExtra("userType", r.getUserType());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            finish();
                        }
                    });
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Đăng ký thất bại!";
                    Toast.makeText(DangKyActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DaiLy>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSubmitRegister.setEnabled(true);
                Toast.makeText(DangKyActivity.this, "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

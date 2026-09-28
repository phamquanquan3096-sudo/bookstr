package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.LoginRequest;
import com.quanly.app.model.LoginResponse;
import com.quanly.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnBackHomeBottom;
    private View btnBackToHome, btnRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBackToHome = findViewById(R.id.btnBackToHome);
        btnBackHomeBottom = findViewById(R.id.btnBackHomeBottom);
        btnRegister = findViewById(R.id.btnRegister);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        View.OnClickListener backHomeListener = v -> navigateBackToHome();

        if (btnBackToHome != null)
            btnBackToHome.setOnClickListener(backHomeListener);
        if (btnBackHomeBottom != null)
            btnBackHomeBottom.setOnClickListener(backHomeListener);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, DangKyActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        navigateBackToHome();
    }

    private void navigateBackToHome() {
        if (isTaskRoot()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        finish();
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        LoginRequest request = new LoginRequest(username, password, "ANY");
        ApiClient.getService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    LoginResponse res = response.body();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công: " + res.getFullName(), Toast.LENGTH_SHORT)
                            .show();

                    SessionManager.saveSession(LoginActivity.this, res.getUserId(), res.getFullName(),
                            res.getUserType());

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("userId", res.getUserId());
                    intent.putExtra("fullName", res.getFullName());
                    intent.putExtra("userType", res.getUserType());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else {
                    String err = response.body() != null ? response.body().getMessage() : "Đăng nhập thất bại!";
                    Toast.makeText(LoginActivity.this, err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Không thể kết nối đến máy chủ: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}

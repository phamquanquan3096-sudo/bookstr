package com.quanly.app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.PhieuCongNo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LapPhieuCongNoActivity extends AppCompatActivity {

    private ImageView btnBackHeader;
    private TextInputLayout tilMaDL, tilSoTienNo;
    private EditText etMaDL, etSoTienNo, etSoTienThu, etGhiChu;
    private Button btnSubmitCongNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_phieu_cong_no);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBackHeader = findViewById(R.id.btnBackHeader);
        tilMaDL = findViewById(R.id.tilMaDL);
        tilSoTienNo = findViewById(R.id.tilSoTienNo);
        etMaDL = findViewById(R.id.etMaDL);
        etSoTienNo = findViewById(R.id.etSoTienNo);
        etSoTienThu = findViewById(R.id.etSoTienThu);
        etGhiChu = findViewById(R.id.etGhiChu);
        btnSubmitCongNo = findViewById(R.id.btnSubmitCongNo);
    }

    private void setupListeners() {
        btnBackHeader.setOnClickListener(v -> finish());

        btnSubmitCongNo.setOnClickListener(v -> submitPhieuCongNo());
    }

    private void submitPhieuCongNo() {
        String maDL = etMaDL.getText().toString().trim();
        String soTienNoStr = etSoTienNo.getText().toString().trim();
        String soTienThuStr = etSoTienThu.getText().toString().trim();
        String ghiChu = etGhiChu.getText().toString().trim();

        if (maDL.isEmpty()) {
            tilMaDL.setError("Vui lòng nhập mã khách hàng!");
            etMaDL.requestFocus();
            return;
        } else {
            tilMaDL.setError(null);
        }

        if (soTienNoStr.isEmpty()) {
            tilSoTienNo.setError("Vui lòng nhập số tiền nợ!");
            etSoTienNo.requestFocus();
            return;
        } else {
            tilSoTienNo.setError(null);
        }

        double soTienNo = 0;
        double soTienThu = 0;
        try {
            soTienNo = Double.parseDouble(soTienNoStr);
            soTienThu = soTienThuStr.isEmpty() ? 0 : Double.parseDouble(soTienThuStr);
        } catch (Exception e) {
            Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitCongNo.setEnabled(false);
        btnSubmitCongNo.setText("Đang lưu...");

        PhieuCongNo newPhieu = new PhieuCongNo();
        newPhieu.setMaDL(maDL);
        newPhieu.setSoTienNo(soTienNo);
        newPhieu.setSoTienThu(soTienThu);
        newPhieu.setConNo(Math.max(0.0, soTienNo - soTienThu));
        newPhieu.setGhiChu(ghiChu.isEmpty() ? "Ghi nhận công nợ" : ghiChu);

        ApiClient.getService().taoPhieuCongNo(newPhieu).enqueue(new Callback<ApiResponse<PhieuCongNo>>() {
            @Override
            public void onResponse(Call<ApiResponse<PhieuCongNo>> call, Response<ApiResponse<PhieuCongNo>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(LapPhieuCongNoActivity.this, "🎉 Đã lập phiếu công nợ thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    btnSubmitCongNo.setEnabled(true);
                    btnSubmitCongNo.setText("📄 XÁC NHẬN LẬP PHIẾU NỢ");
                    String msg = response.body() != null ? response.body().getMessage() : "Không thể tạo phiếu";
                    Toast.makeText(LapPhieuCongNoActivity.this, "❌ " + msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PhieuCongNo>> call, Throwable t) {
                btnSubmitCongNo.setEnabled(true);
                btnSubmitCongNo.setText("📄 XÁC NHẬN LẬP PHIẾU NỢ");
                Toast.makeText(LapPhieuCongNoActivity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

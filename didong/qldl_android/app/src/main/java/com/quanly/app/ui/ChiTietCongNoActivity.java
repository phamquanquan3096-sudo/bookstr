package com.quanly.app.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.PhieuCongNo;

import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietCongNoActivity extends AppCompatActivity {

    private ImageView btnBackHeader;
    private Button btnDeleteCongNo;
    private TextView tvMaPCN, tvMaDL, tvTrangThaiBadge;
    private TextView tvTongNo, tvDaThu, tvConNo, tvTienDoPercent, tvGhiChu;
    private ProgressBar pbTienDo;
    private View cardThuTien;
    private TextInputLayout tilSoTienThuMoi;
    private EditText etSoTienThuMoi;
    private Button btnThuToanBo, btnSubmitThuTien;

    private PhieuCongNo phieuCongNo;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_cong_no);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        phieuCongNo = (PhieuCongNo) getIntent().getSerializableExtra("phieuCongNo");
        if (phieuCongNo == null) {
            Toast.makeText(this, "Không tìm thấy thông tin phiếu công nợ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        btnBackHeader = findViewById(R.id.btnBackHeader);
        btnDeleteCongNo = findViewById(R.id.btnDeleteCongNo);
        tvMaPCN = findViewById(R.id.tvMaPCN);
        tvMaDL = findViewById(R.id.tvMaDL);
        tvTrangThaiBadge = findViewById(R.id.tvTrangThaiBadge);
        tvTongNo = findViewById(R.id.tvTongNo);
        tvDaThu = findViewById(R.id.tvDaThu);
        tvConNo = findViewById(R.id.tvConNo);
        tvTienDoPercent = findViewById(R.id.tvTienDoPercent);
        tvGhiChu = findViewById(R.id.tvGhiChu);
        pbTienDo = findViewById(R.id.pbTienDo);

        cardThuTien = findViewById(R.id.cardThuTien);
        tilSoTienThuMoi = findViewById(R.id.tilSoTienThuMoi);
        etSoTienThuMoi = findViewById(R.id.etSoTienThuMoi);
        btnThuToanBo = findViewById(R.id.btnThuToanBo);
        btnSubmitThuTien = findViewById(R.id.btnSubmitThuTien);
    }

    private void bindData() {
        if (phieuCongNo == null) return;

        String maPCN = phieuCongNo.getMaPhieuCN() != null ? phieuCongNo.getMaPhieuCN() : "--";
        String maDL = phieuCongNo.getMaDL() != null ? phieuCongNo.getMaDL() : "--";
        double soTienNo = phieuCongNo.getSoTienNo() != null ? phieuCongNo.getSoTienNo() : 0.0;
        double soTienThu = phieuCongNo.getSoTienThu() != null ? phieuCongNo.getSoTienThu() : 0.0;
        double conNo = phieuCongNo.getConNo() != null ? phieuCongNo.getConNo() : Math.max(0.0, soTienNo - soTienThu);

        tvMaPCN.setText("Mã Phiếu: " + maPCN);
        tvMaDL.setText("👤 Khách hàng: " + maDL);
        tvTongNo.setText(formatter.format(soTienNo) + " đ");
        tvDaThu.setText(formatter.format(soTienThu) + " đ");
        tvConNo.setText(formatter.format(conNo) + " đ");

        if (phieuCongNo.getGhiChu() != null && !phieuCongNo.getGhiChu().trim().isEmpty()) {
            tvGhiChu.setText("📝 Ghi chú: " + phieuCongNo.getGhiChu());
        } else {
            tvGhiChu.setText("📝 Ghi chú: Không có ghi chú");
        }

        int progress = 0;
        if (soTienNo > 0) {
            progress = (int) Math.min(100, Math.round((soTienThu / soTienNo) * 100));
        } else if (conNo <= 0) {
            progress = 100;
        }

        pbTienDo.setProgress(progress);
        tvTienDoPercent.setText(progress + "%");

        if (conNo <= 0) {
            tvTrangThaiBadge.setText("ĐÃ THU HẾT");
            tvTrangThaiBadge.setTextColor(Color.parseColor("#2E7D32"));
            tvTrangThaiBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            cardThuTien.setVisibility(View.GONE);
        } else {
            tvTrangThaiBadge.setText("ĐANG CÒN NỢ (" + (100 - progress) + "%)");
            tvTrangThaiBadge.setTextColor(Color.parseColor("#D32F2F"));
            tvTrangThaiBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
            cardThuTien.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnBackHeader.setOnClickListener(v -> finish());

        double conNo = phieuCongNo.getConNo() != null ? phieuCongNo.getConNo() : 0.0;
        btnThuToanBo.setOnClickListener(v -> etSoTienThuMoi.setText(String.valueOf((long) conNo)));

        btnSubmitThuTien.setOnClickListener(v -> submitThuTien());

        btnDeleteCongNo.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa phiếu nợ")
                    .setMessage("Bạn có chắc chắn muốn xóa phiếu công nợ #" + phieuCongNo.getMaPhieuCN() + " khỏi hệ thống?")
                    .setPositiveButton("Xóa ngay", (dialog, which) -> deletePhieu())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void submitThuTien() {
        String thuStr = etSoTienThuMoi.getText().toString().trim();
        if (thuStr.isEmpty()) {
            tilSoTienThuMoi.setError("Vui lòng nhập số tiền thu!");
            etSoTienThuMoi.requestFocus();
            return;
        } else {
            tilSoTienThuMoi.setError(null);
        }

        double soThuMoi = 0;
        try {
            soThuMoi = Double.parseDouble(thuStr);
        } catch (Exception e) {
            Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (soThuMoi <= 0) {
            tilSoTienThuMoi.setError("Số tiền thu phải lớn hơn 0!");
            etSoTienThuMoi.requestFocus();
            return;
        }

        btnSubmitThuTien.setEnabled(false);
        btnSubmitThuTien.setText("Đang xử lý...");

        ApiClient.getService().thuTienCongNo(phieuCongNo.getMaPhieuCN(), soThuMoi).enqueue(new Callback<ApiResponse<PhieuCongNo>>() {
            @Override
            public void onResponse(Call<ApiResponse<PhieuCongNo>> call, Response<ApiResponse<PhieuCongNo>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ChiTietCongNoActivity.this, "🎉 Đã thu tiền công nợ thành công!", Toast.LENGTH_SHORT).show();
                    phieuCongNo = response.body().getData();
                    bindData();
                    etSoTienThuMoi.setText("");
                    btnSubmitThuTien.setEnabled(true);
                    btnSubmitThuTien.setText("💰 XÁC NHẬN THU TIỀN CÔNG NỢ");
                    setResult(RESULT_OK);
                } else {
                    btnSubmitThuTien.setEnabled(true);
                    btnSubmitThuTien.setText("💰 XÁC NHẬN THU TIỀN CÔNG NỢ");
                    String msg = response.body() != null ? response.body().getMessage() : "Thu tiền thất bại";
                    Toast.makeText(ChiTietCongNoActivity.this, "❌ " + msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PhieuCongNo>> call, Throwable t) {
                btnSubmitThuTien.setEnabled(true);
                btnSubmitThuTien.setText("💰 XÁC NHẬN THU TIỀN CÔNG NỢ");
                Toast.makeText(ChiTietCongNoActivity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePhieu() {
        ApiClient.getService().deleteCongNo(phieuCongNo.getMaPhieuCN()).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ChiTietCongNoActivity.this, "Đã xóa phiếu công nợ thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ChiTietCongNoActivity.this, "Không thể xóa phiếu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(ChiTietCongNoActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

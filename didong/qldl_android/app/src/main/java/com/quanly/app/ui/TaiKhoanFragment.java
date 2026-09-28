package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.quanly.app.R;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.DaiLy;
import com.quanly.app.model.NhanVien;
import com.quanly.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaiKhoanFragment extends Fragment {

    private String userId, fullName, userType;
    private TextView tvFullName, tvUserRole, tvUserId, tvUserType, tvPhone, tvAddress, tvEmail;
    private Button btnEditProfile;
    private DaiLy currentDaiLy;
    private NhanVien currentNhanVien;

    public static TaiKhoanFragment newInstance(String userId, String fullName, String userType) {
        TaiKhoanFragment fragment = new TaiKhoanFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("fullName", fullName);
        args.putString("userType", userType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tai_khoan, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            fullName = getArguments().getString("fullName");
            userType = getArguments().getString("userType");
        }

        if ((userId == null || userId.isEmpty()) && getContext() != null && SessionManager.isLoggedIn(getContext())) {
            userId = SessionManager.getUserId(getContext());
            fullName = SessionManager.getFullName(getContext());
            userType = SessionManager.getUserType(getContext());
        }

        tvFullName = view.findViewById(R.id.tvFullName);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        tvUserId = view.findViewById(R.id.tvUserId);
        tvUserType = view.findViewById(R.id.tvUserType);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        View cardAdminActions = view.findViewById(R.id.cardAdminActions);

        Button btnManageCongNo = view.findViewById(R.id.btnManageCongNo);
        Button btnManageKho = view.findViewById(R.id.btnManageKho);
        Button btnRevenueStats = view.findViewById(R.id.btnRevenueStats);
        Button btnManageStaff = view.findViewById(R.id.btnManageStaff);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        String role = (userType != null) ? userType.trim().toUpperCase() : "";
        boolean isKeToan = role.contains("NVKT") || role.contains("KE_TOAN") || role.contains("KETOAN") || role.contains("KẾ TOÁN") || role.equals("KT");
        boolean isAdmin = !isKeToan && (role.contains("QTHT") || role.equals("ADMIN") || role.contains("QUẢN TRỊ") || role.contains("QUAN_TRI"));
        boolean isKinhDoanh = !isKeToan && (role.contains("NVKD") || role.contains("KINH_DOANH") || role.contains("KINHDOANH"));
        boolean isKho = !isKeToan && !isAdmin && (role.contains("NVK") || role.contains("THU_KHO") || role.contains("THUKHO") || role.equals("KHO"));
        boolean isGiaoHang = !isKeToan && (role.contains("NVGH") || role.contains("GIAO_HANG") || role.contains("GIAOHANG"));

        String roleTitle = "Khách hàng";
        if (isAdmin)
            roleTitle = "Quản trị viên (Admin)";
        else if (isKinhDoanh)
            roleTitle = "Nhân viên Kinh doanh";
        else if (isKeToan)
            roleTitle = "Nhân viên Kế toán";
        else if (isKho)
            roleTitle = "Thủ kho";
        else if (isGiaoHang)
            roleTitle = "Nhân viên Giao hàng";

        tvFullName.setText(fullName != null && !fullName.isEmpty() ? fullName : roleTitle);
        tvUserId.setText("Mã tài khoản: " + (userId != null ? userId : "--"));
        tvUserType.setText("Loại tài khoản: " + roleTitle);
        tvUserRole.setText("Vai trò: " + roleTitle);

        // Hiển thị phần Tùy chọn Quản trị viên theo từng vai trò
        if (isAdmin) {
            cardAdminActions.setVisibility(View.VISIBLE);
            if (btnManageCongNo != null)
                btnManageCongNo.setVisibility(View.VISIBLE);
            if (btnManageKho != null)
                btnManageKho.setVisibility(View.VISIBLE);
            if (btnRevenueStats != null)
                btnRevenueStats.setVisibility(View.VISIBLE);
            if (btnManageStaff != null)
                btnManageStaff.setVisibility(View.VISIBLE);
        } else if (isKeToan) {
            cardAdminActions.setVisibility(View.VISIBLE);
            if (btnManageCongNo != null)
                btnManageCongNo.setVisibility(View.VISIBLE);
            if (btnManageKho != null)
                btnManageKho.setVisibility(View.GONE); // TUYỆT ĐỐI ẨN KHO HÀNG
            if (btnRevenueStats != null)
                btnRevenueStats.setVisibility(View.VISIBLE);
            if (btnManageStaff != null)
                btnManageStaff.setVisibility(View.GONE);
        } else if (isKho) {
            cardAdminActions.setVisibility(View.VISIBLE);
            if (btnManageCongNo != null)
                btnManageCongNo.setVisibility(View.GONE);
            if (btnManageKho != null)
                btnManageKho.setVisibility(View.VISIBLE);
            if (btnRevenueStats != null)
                btnRevenueStats.setVisibility(View.GONE);
            if (btnManageStaff != null)
                btnManageStaff.setVisibility(View.GONE);
        } else if (isKinhDoanh) {
            cardAdminActions.setVisibility(View.VISIBLE);
            if (btnManageCongNo != null)
                btnManageCongNo.setVisibility(View.VISIBLE);
            if (btnManageKho != null)
                btnManageKho.setVisibility(View.GONE);
            if (btnRevenueStats != null)
                btnRevenueStats.setVisibility(View.VISIBLE);
            if (btnManageStaff != null)
                btnManageStaff.setVisibility(View.GONE);
        } else {
            cardAdminActions.setVisibility(View.GONE);
        }

        // Bắt sự kiện bấm nút Chỉnh sửa thông tin cá nhân -> Mở Activity chỉnh sửa chuyên biệt
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), ChinhSuaThongTinActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("fullName", fullName);
                    intent.putExtra("userType", userType);
                    startActivity(intent);
                }
            });
        }

        // Tải chi tiết thông tin tài khoản từ Server
        loadProfileInfo();

        // Nút Quản lý Công nợ (Admin)
        if (btnManageCongNo != null) {
            btnManageCongNo.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedTab(R.id.nav_cong_no);
                } else if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, CongNoFragment.newInstance(userId))
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // Nút Danh sách Kho hàng (Admin)
        if (btnManageKho != null) {
            btnManageKho.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedTab(R.id.nav_kho);
                } else if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new KhoFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // Nút Thống Kê Doanh Thu (Admin)
        if (btnRevenueStats != null) {
            btnRevenueStats.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new ThongKeFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // Nút Quản lý Nhân viên (Admin)
        if (btnManageStaff != null) {
            btnManageStaff.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new NhanVienFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // Xử lý nút ĐĂNG XUẤT
        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(getContext());
            Toast.makeText(getContext(), "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileInfo();
    }

    private void loadProfileInfo() {
        if (userId == null || userId.isEmpty())
            return;

        boolean isStaff = userType != null && (userType.contains("QTHT") || userType.contains("NVKD")
                || userType.contains("NVKT") || userType.contains("NVK") || userType.contains("KHO")
                || userType.contains("NVGH") || userType.contains("GIAO_HANG")
                || userType.equalsIgnoreCase("ADMIN") || userType.contains("STAFF"));

        if (isStaff) {
            // Tải thông tin Nhân viên / Admin
            ApiClient.getApiService().getNhanVienByMa(userId).enqueue(new Callback<ApiResponse<NhanVien>>() {
                @Override
                public void onResponse(Call<ApiResponse<NhanVien>> call, Response<ApiResponse<NhanVien>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        currentNhanVien = response.body().getData();
                        if (currentNhanVien.getTenNV() != null && !currentNhanVien.getTenNV().isEmpty()) {
                            fullName = currentNhanVien.getTenNV();
                            tvFullName.setText(fullName);
                        }
                        if (tvPhone != null)
                            tvPhone.setText("📞 Số điện thoại: "
                                    + (currentNhanVien.getSdt() != null && !currentNhanVien.getSdt().isEmpty()
                                            ? currentNhanVien.getSdt()
                                            : "Chưa cập nhật"));
                        if (tvAddress != null)
                            tvAddress.setText("📍 Địa chỉ: Trụ sở công ty / Chi nhánh");
                        if (tvEmail != null)
                            tvEmail.setText(
                                    "✉️ Email: " + (currentNhanVien.getEmail() != null && !currentNhanVien.getEmail().isEmpty()
                                            ? currentNhanVien.getEmail()
                                            : "Chưa cập nhật"));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<NhanVien>> call, Throwable t) {
                }
            });
        } else {
            // Tải thông tin Khách hàng / Đại lý
            ApiClient.getApiService().getDaiLyDetail(userId).enqueue(new Callback<ApiResponse<DaiLy>>() {
                @Override
                public void onResponse(Call<ApiResponse<DaiLy>> call, Response<ApiResponse<DaiLy>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        currentDaiLy = response.body().getData();
                        if (currentDaiLy.getTenDL() != null && !currentDaiLy.getTenDL().isEmpty()) {
                            fullName = currentDaiLy.getTenDL();
                            tvFullName.setText(fullName);
                        }
                        if (tvPhone != null)
                            tvPhone.setText("📞 Số điện thoại: "
                                    + (currentDaiLy.getSdt() != null && !currentDaiLy.getSdt().isEmpty()
                                            ? currentDaiLy.getSdt()
                                            : "Chưa cập nhật"));
                        if (tvAddress != null)
                            tvAddress.setText("📍 Địa chỉ: "
                                    + (currentDaiLy.getDiaChi() != null && !currentDaiLy.getDiaChi().isEmpty()
                                            ? currentDaiLy.getDiaChi()
                                            : "Chưa cập nhật"));
                        if (tvEmail != null)
                            tvEmail.setText(
                                    "✉️ Email: " + (currentDaiLy.getEmail() != null && !currentDaiLy.getEmail().isEmpty()
                                            ? currentDaiLy.getEmail()
                                            : "Chưa cập nhật"));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<DaiLy>> call, Throwable t) {
                }
            });
        }
    }

}

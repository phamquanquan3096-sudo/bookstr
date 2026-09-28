package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.quanly.app.R;
import com.quanly.app.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private String userId, fullName, userType;
    private long backPressedTime = 0;
    private Toast backToast;
    private BottomNavigationView bottomNav;
    private int currentTabId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        bottomNav = findViewById(R.id.bottomNavigation);
        reloadSessionData();
        updateMenuAndPermissions();

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentTabId) {
                return true;
            }
            switchTab(itemId);
            return true;
        });

        if (savedInstanceState == null) {
            selectDefaultTabForRole();
        }
    }

    public void setSelectedTab(int itemId) {
        if (bottomNav != null && bottomNav.getSelectedItemId() != itemId) {
            bottomNav.setSelectedItemId(itemId);
        }
        switchTab(itemId);
    }

    private void switchTab(int itemId) {
        if (itemId == currentTabId && getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) != null) {
            return;
        }
        Fragment selectedFragment = null;
        if (itemId == R.id.nav_san_pham) {
            selectedFragment = SanPhamFragment.newInstance(userId, userType);
        } else if (itemId == R.id.nav_don_hang) {
            selectedFragment = DonHangFragment.newInstance(userId, userType);
        } else if (itemId == R.id.nav_cong_no) {
            selectedFragment = CongNoFragment.newInstance(userId);
        } else if (itemId == R.id.nav_kho) {
            selectedFragment = new KhoFragment();
        } else if (itemId == R.id.nav_thong_ke) {
            if (userId != null && !userId.isEmpty()) {
                selectedFragment = TaiKhoanFragment.newInstance(userId, fullName, userType);
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                return;
            }
        }

        if (selectedFragment != null) {
            currentTabId = itemId;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, selectedFragment)
                    .commitAllowingStateLoss();
        }
    }

    private void selectDefaultTabForRole() {
        boolean isGiaoHang = userType != null && (userType.contains("NVGH") || userType.contains("GIAO_HANG"));
        boolean isKho = userType != null && (userType.contains("NVK") || userType.contains("KHO") || userType.contains("THU_KHO"));
        boolean isKeToan = userType != null && (userType.contains("NVKT") || userType.contains("KE_TOAN"));
        int defaultTab = (isGiaoHang || isKeToan) ? R.id.nav_don_hang : (isKho ? R.id.nav_kho : R.id.nav_san_pham);
        setSelectedTab(defaultTab);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        reloadSessionData();
        updateMenuAndPermissions();
        selectDefaultTabForRole();
    }

    private void reloadSessionData() {
        userId = getIntent().getStringExtra("userId");
        fullName = getIntent().getStringExtra("fullName");
        userType = getIntent().getStringExtra("userType");

        if ((userId == null || userId.isEmpty()) && SessionManager.isLoggedIn(this)) {
            userId = SessionManager.getUserId(this);
            fullName = SessionManager.getFullName(this);
            userType = SessionManager.getUserType(this);
        } else if (userId != null && !userId.isEmpty()) {
            SessionManager.saveSession(this, userId, fullName, userType);
        }
    }

    private void updateMenuAndPermissions() {
        if (bottomNav == null) return;

        String role = (userType != null) ? userType.trim().toUpperCase() : "";
        boolean isKeToan = role.contains("NVKT") || role.contains("KE_TOAN") || role.contains("KETOAN") || role.contains("KẾ TOÁN") || role.equals("KT");
        boolean isAdmin = !isKeToan && (role.contains("QTHT") || role.equals("ADMIN") || role.contains("QUẢN TRỊ") || role.contains("QUAN_TRI"));
        boolean isKinhDoanh = !isKeToan && (role.contains("NVKD") || role.contains("KINH_DOANH") || role.contains("KINHDOANH"));
        boolean isKho = !isKeToan && !isAdmin && (role.contains("NVK") || role.contains("THU_KHO") || role.contains("THUKHO") || role.equals("KHO"));

        MenuItem itemSanPham = bottomNav.getMenu().findItem(R.id.nav_san_pham);
        MenuItem itemDonHang = bottomNav.getMenu().findItem(R.id.nav_don_hang);
        MenuItem itemCongNo = bottomNav.getMenu().findItem(R.id.nav_cong_no);
        MenuItem itemKho = bottomNav.getMenu().findItem(R.id.nav_kho);
        MenuItem itemLast = bottomNav.getMenu().findItem(R.id.nav_thong_ke);

        // 1. Tab Sản Phẩm & Đơn Hàng luôn hiển thị
        if (itemSanPham != null) itemSanPham.setVisible(true);
        if (itemDonHang != null) itemDonHang.setVisible(true);

        // 2. Tab Công Nợ: Chỉ hiện với Admin, Kinh doanh, Kế toán
        if (itemCongNo != null) {
            itemCongNo.setVisible(isAdmin || isKinhDoanh || isKeToan);
        }

        // 3. Tab Kho: Chỉ hiện với Admin, Kho (TUYỆT ĐỐI ẨN đối với Kế toán)
        if (itemKho != null) {
            itemKho.setVisible(!isKeToan && (isAdmin || isKho));
        }

        // 4. Tab Tài khoản / Đăng nhập
        if (itemLast != null) {
            if (userId != null && !userId.isEmpty()) {
                itemLast.setTitle("Tài khoản");
            } else {
                itemLast.setTitle("Đăng nhập");
            }
            itemLast.setVisible(true);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }

        String role = (userType != null) ? userType.trim().toUpperCase() : "";
        boolean isKeToan = role.contains("NVKT") || role.contains("KE_TOAN") || role.contains("KETOAN") || role.contains("KẾ TOÁN") || role.equals("KT");
        boolean isGiaoHang = !isKeToan && (role.contains("NVGH") || role.contains("GIAO_HANG") || role.contains("GIAOHANG"));
        boolean isKho = !isKeToan && (role.contains("NVK") || role.contains("KHO") || role.contains("THU_KHO") || role.contains("THUKHO"));
        int homeTab = (isGiaoHang || isKeToan) ? R.id.nav_don_hang : (isKho ? R.id.nav_kho : R.id.nav_san_pham);
        if (bottomNav != null && bottomNav.getSelectedItemId() != homeTab) {
            bottomNav.setSelectedItemId(homeTab);
            return;
        }

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null)
                backToast.cancel();
            super.onBackPressed();
        } else {
            backToast = Toast.makeText(MainActivity.this, "Nhấn lần nữa để thoát ứng dụng", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadSessionData();
        updateMenuAndPermissions();
    }

    @Override
    public void finish() {
        super.finish();
    }
}

package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.quanly.app.R;
import com.quanly.app.adapter.DonHangAdapter;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.DonHang;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonHangFragment extends Fragment {

    private RecyclerView rvDonHang;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabTaoDon;
    private TabLayout tabLayoutPayment;
    private TextView tvEmpty;
    private DonHangAdapter adapter;
    private String userId, userType;
    private List<DonHang> allOrdersList = new ArrayList<>();
    private int currentFilterIndex = 0; // 0: Tất cả, 1: Chưa thu tiền, 2: Đã thu tiền

    public static DonHangFragment newInstance(String userId, String userType) {
        DonHangFragment fragment = new DonHangFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("userType", userType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_don_hang, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            userType = getArguments().getString("userType");
        }

        rvDonHang = view.findViewById(R.id.rvDonHang);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        fabTaoDon = view.findViewById(R.id.fabTaoDon);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tabLayoutPayment = view.findViewById(R.id.tabLayoutPayment);

        rvDonHang.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DonHangAdapter();
        adapter.setUserType(userType);
        rvDonHang.setAdapter(adapter);

        setupTabs();

        swipeRefresh.setOnRefreshListener(this::loadData);

        fabTaoDon.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), TaoDonHangActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        loadData();
        return view;
    }

    private void setupTabs() {
        if (tabLayoutPayment == null) return;
        tabLayoutPayment.removeAllTabs();
        tabLayoutPayment.addTab(tabLayoutPayment.newTab().setText("Tất cả"));
        tabLayoutPayment.addTab(tabLayoutPayment.newTab().setText("Chưa thu tiền"));
        tabLayoutPayment.addTab(tabLayoutPayment.newTab().setText("Đã thu tiền"));

        tabLayoutPayment.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilterIndex = tab.getPosition();
                applyCurrentFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void applyCurrentFilter() {
        if (allOrdersList == null) {
            adapter.setList(new ArrayList<>());
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        List<DonHang> filtered = new ArrayList<>();
        for (DonHang dh : allOrdersList) {
            String tt = dh.getTinhTrangThanhToan();
            boolean isPaid = "Đã thanh toán".equalsIgnoreCase(tt);

            if (currentFilterIndex == 1) {
                // Chưa thu tiền
                if (!isPaid) {
                    filtered.add(dh);
                }
            } else if (currentFilterIndex == 2) {
                // Đã thu tiền
                if (isPaid) {
                    filtered.add(dh);
                }
            } else {
                // Tất cả
                filtered.add(dh);
            }
        }

        adapter.setList(filtered);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void loadData() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);

        boolean isStaff = userType != null && (userType.contains("QTHT") || userType.contains("NVKD") || userType.contains("NVKT") || userType.contains("NVK") || userType.contains("KHO") || userType.contains("NVGH") || userType.contains("GIAO_HANG") || userType.equalsIgnoreCase("ADMIN") || userType.contains("KE_TOAN"));
        boolean isGiaoHang = userType != null && (userType.contains("NVGH") || userType.contains("GIAO_HANG"));
        boolean isKeToan = userType != null && (userType.contains("NVKT") || userType.contains("KE_TOAN"));
        boolean canCreateOrder = userType != null && (userType.contains("QTHT") || userType.contains("NVKD") || userType.equalsIgnoreCase("ADMIN")) && !isKeToan;
        
        String maDL = null;
        String maGH = null;

        if (isGiaoHang) {
            if (fabTaoDon != null) fabTaoDon.setVisibility(View.GONE);
        } else if (!isStaff) {
            maDL = (userId != null && !userId.isEmpty()) ? userId : "DL01";
            if (fabTaoDon != null) fabTaoDon.setVisibility(View.GONE);
        } else {
            if (fabTaoDon != null) fabTaoDon.setVisibility(canCreateOrder ? View.VISIBLE : View.GONE);
        }

        ApiClient.getService().getDonHangs(maDL, maGH, null).enqueue(new Callback<ApiResponse<List<DonHang>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DonHang>>> call, Response<ApiResponse<List<DonHang>>> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    allOrdersList = response.body().getData();
                    if (allOrdersList == null) allOrdersList = new ArrayList<>();
                    applyCurrentFilter();
                } else {
                    allOrdersList = new ArrayList<>();
                    applyCurrentFilter();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DonHang>>> call, Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                allOrdersList = new ArrayList<>();
                applyCurrentFilter();
            }
        });
    }
}

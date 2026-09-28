package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.quanly.app.R;
import com.quanly.app.adapter.CongNoAdapter;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.PhieuCongNo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CongNoFragment extends Fragment implements CongNoAdapter.OnCongNoActionListener {

    private RecyclerView rvCongNo;
    private SwipeRefreshLayout swipeRefreshCongNo;
    private EditText etSearchCongNo;
    private ExtendedFloatingActionButton fabAddCongNo;
    private Button btnHeaderAddCongNo;
    private TextView tvDashboardTongNo, tvDashboardDaThu, tvDashboardConNo;

    private CongNoAdapter adapter;
    private List<PhieuCongNo> rawList = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private String userId;

    public static CongNoFragment newInstance(String userId) {
        CongNoFragment fragment = new CongNoFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cong_no, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        rvCongNo = view.findViewById(R.id.rvCongNo);
        swipeRefreshCongNo = view.findViewById(R.id.swipeRefreshCongNo);
        etSearchCongNo = view.findViewById(R.id.etSearchCongNo);
        fabAddCongNo = view.findViewById(R.id.fabAddCongNo);
        btnHeaderAddCongNo = view.findViewById(R.id.btnHeaderAddCongNo);

        tvDashboardTongNo = view.findViewById(R.id.tvDashboardTongNo);
        tvDashboardDaThu = view.findViewById(R.id.tvDashboardDaThu);
        tvDashboardConNo = view.findViewById(R.id.tvDashboardConNo);

        rvCongNo.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CongNoAdapter(this);
        rvCongNo.setAdapter(adapter);

        if (swipeRefreshCongNo != null) {
            swipeRefreshCongNo.setOnRefreshListener(this::loadData);
        }

        // Bắt sự kiện Lập phiếu nợ từ FAB -> Mở LapPhieuCongNoActivity
        if (fabAddCongNo != null) {
            fabAddCongNo.setOnClickListener(v -> openLapPhieuCongNo());
        }

        // Bắt sự kiện Lập phiếu nợ từ Header -> Mở LapPhieuCongNoActivity
        if (btnHeaderAddCongNo != null) {
            btnHeaderAddCongNo.setOnClickListener(v -> openLapPhieuCongNo());
        }

        // Tìm kiếm công nợ theo thời gian thực
        if (etSearchCongNo != null) {
            etSearchCongNo.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterCongNo(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void openLapPhieuCongNo() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), LapPhieuCongNoActivity.class);
            startActivity(intent);
        }
    }

    public void loadData() {
        if (swipeRefreshCongNo != null) {
            swipeRefreshCongNo.setRefreshing(true);
        }

        ApiClient.getService().getCongNo(null).enqueue(new Callback<ApiResponse<List<PhieuCongNo>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PhieuCongNo>>> call, Response<ApiResponse<List<PhieuCongNo>>> response) {
                if (swipeRefreshCongNo != null) {
                    swipeRefreshCongNo.setRefreshing(false);
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    rawList = response.body().getData();
                    if (rawList == null) rawList = new ArrayList<>();
                    updateDashboard(rawList);
                    filterCongNo(etSearchCongNo != null ? etSearchCongNo.getText().toString() : "");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PhieuCongNo>>> call, Throwable t) {
                if (swipeRefreshCongNo != null) {
                    swipeRefreshCongNo.setRefreshing(false);
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải công nợ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateDashboard(List<PhieuCongNo> list) {
        double totalNo = 0.0;
        double totalThu = 0.0;
        double totalConNo = 0.0;

        for (PhieuCongNo p : list) {
            double no = p.getSoTienNo() != null ? p.getSoTienNo() : 0.0;
            double thu = p.getSoTienThu() != null ? p.getSoTienThu() : 0.0;
            double con = p.getConNo() != null ? p.getConNo() : Math.max(0.0, no - thu);

            totalNo += no;
            totalThu += thu;
            totalConNo += con;
        }

        if (tvDashboardTongNo != null) tvDashboardTongNo.setText(formatter.format(totalNo) + " đ");
        if (tvDashboardDaThu != null) tvDashboardDaThu.setText(formatter.format(totalThu) + " đ");
        if (tvDashboardConNo != null) tvDashboardConNo.setText(formatter.format(totalConNo) + " đ");
    }

    private void filterCongNo(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            adapter.setList(rawList);
            return;
        }

        String query = keyword.trim().toLowerCase();
        List<PhieuCongNo> filtered = new ArrayList<>();
        for (PhieuCongNo p : rawList) {
            boolean matchMaPCN = p.getMaPhieuCN() != null && p.getMaPhieuCN().toLowerCase().contains(query);
            boolean matchMaDL = p.getMaDL() != null && p.getMaDL().toLowerCase().contains(query);
            boolean matchGhiChu = p.getGhiChu() != null && p.getGhiChu().toLowerCase().contains(query);

            if (matchMaPCN || matchMaDL || matchGhiChu) {
                filtered.add(p);
            }
        }
        adapter.setList(filtered);
    }

    // ========================================================
    // CHỨC NĂNG 1: THU TIỀN CÔNG NỢ -> MỞ ChiTietCongNoActivity
    // ========================================================
    @Override
    public void onThuTien(PhieuCongNo pcn) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), ChiTietCongNoActivity.class);
            intent.putExtra("phieuCongNo", pcn);
            startActivity(intent);
        }
    }

    // ========================================================
    // CHỨC NĂNG 2: XEM CHI TIẾT CÔNG NỢ -> MỞ ChiTietCongNoActivity
    // ========================================================
    @Override
    public void onChiTiet(PhieuCongNo pcn) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), ChiTietCongNoActivity.class);
            intent.putExtra("phieuCongNo", pcn);
            startActivity(intent);
        }
    }
}

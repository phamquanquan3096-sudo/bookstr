package com.quanly.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.quanly.app.R;
import com.quanly.app.adapter.DonHangAdapter;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.DonHang;
import com.quanly.app.model.RevenueStats;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThongKeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshThongKe;
    private TextView tvTongDoanhThu, tvTongDon, tvDaThanhToan, tvDangNo, tvChuaThanhToan;
    private CardView cardTongDon, cardDaThanhToan, cardDangNo, cardChuaThanhToan;
    private Spinner spnThang;
    private RecyclerView rvDonHangThongKe;

    private DonHangAdapter donHangAdapter;
    private List<DonHang> rawOrderList = new ArrayList<>();
    private String currentStatusFilter = "ALL"; // "ALL", "DA_THANH_TOAN", "DANG_NO", "CHUA_THANH_TOAN"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_thong_ke, container, false);

        swipeRefreshThongKe = v.findViewById(R.id.swipeRefreshThongKe);
        tvTongDoanhThu = v.findViewById(R.id.tvTongDoanhThu);
        tvTongDon = v.findViewById(R.id.tvTongDon);
        tvDaThanhToan = v.findViewById(R.id.tvDaThanhToan);
        tvDangNo = v.findViewById(R.id.tvDangNo);
        tvChuaThanhToan = v.findViewById(R.id.tvChuaThanhToan);

        cardTongDon = v.findViewById(R.id.cardTongDon);
        cardDaThanhToan = v.findViewById(R.id.cardDaThanhToan);
        cardDangNo = v.findViewById(R.id.cardDangNo);
        cardChuaThanhToan = v.findViewById(R.id.cardChuaThanhToan);

        spnThang = v.findViewById(R.id.spnThang);
        rvDonHangThongKe = v.findViewById(R.id.rvDonHangThongKe);

        rvDonHangThongKe.setLayoutManager(new LinearLayoutManager(getContext()));
        donHangAdapter = new DonHangAdapter();
        rvDonHangThongKe.setAdapter(donHangAdapter);

        if (cardTongDon != null) cardTongDon.setOnClickListener(v1 -> selectFilter("ALL"));
        if (cardDaThanhToan != null) cardDaThanhToan.setOnClickListener(v1 -> selectFilter("DA_THANH_TOAN"));
        if (cardDangNo != null) cardDangNo.setOnClickListener(v1 -> selectFilter("DANG_NO"));
        if (cardChuaThanhToan != null) cardChuaThanhToan.setOnClickListener(v1 -> selectFilter("CHUA_THANH_TOAN"));

        setupMonthSpinner();

        swipeRefreshThongKe.setOnRefreshListener(this::loadData);

        loadData();

        return v;
    }

    private void selectFilter(String filter) {
        currentStatusFilter = filter;
        updateCardSelectionUI();
        applyFilterAndDisplay();
    }

    private void updateCardSelectionUI() {
        if (cardTongDon != null) cardTongDon.setAlpha("ALL".equals(currentStatusFilter) ? 1.0f : 0.6f);
        if (cardDaThanhToan != null) cardDaThanhToan.setAlpha("DA_THANH_TOAN".equals(currentStatusFilter) ? 1.0f : 0.6f);
        if (cardDangNo != null) cardDangNo.setAlpha("DANG_NO".equals(currentStatusFilter) ? 1.0f : 0.6f);
        if (cardChuaThanhToan != null) cardChuaThanhToan.setAlpha("CHUA_THANH_TOAN".equals(currentStatusFilter) ? 1.0f : 0.6f);
    }

    private void applyFilterAndDisplay() {
        if (rawOrderList == null) {
            donHangAdapter.setList(new ArrayList<>());
            return;
        }
        if ("ALL".equals(currentStatusFilter)) {
            donHangAdapter.setList(new ArrayList<>(rawOrderList));
            return;
        }
        List<DonHang> filtered = new ArrayList<>();
        for (DonHang dh : rawOrderList) {
            String tt = dh.getTinhTrangThanhToan();
            if ("DA_THANH_TOAN".equals(currentStatusFilter)) {
                if ("Đã thanh toán".equalsIgnoreCase(tt)) {
                    filtered.add(dh);
                }
            } else if ("DANG_NO".equals(currentStatusFilter)) {
                if ("Đang nợ".equalsIgnoreCase(tt)) {
                    filtered.add(dh);
                }
            } else if ("CHUA_THANH_TOAN".equals(currentStatusFilter)) {
                if (tt == null || "Chưa thanh toán".equalsIgnoreCase(tt) || (!"Đã thanh toán".equalsIgnoreCase(tt) && !"Đang nợ".equalsIgnoreCase(tt))) {
                    filtered.add(dh);
                }
            }
        }
        donHangAdapter.setList(filtered);
    }

    private void setupMonthSpinner() {
        List<String> months = new ArrayList<>();
        months.add("Tất cả các tháng");
        for (int i = 1; i <= 12; i++) {
            months.add("Tháng " + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, months);
        spnThang.setAdapter(adapter);

        spnThang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    loadDonHangs(null);
                } else {
                    loadDonHangsByMonth(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadData() {
        loadRevenueStats();
        int selectedPos = spnThang.getSelectedItemPosition();
        if (selectedPos <= 0) {
            loadDonHangs(null);
        } else {
            loadDonHangsByMonth(selectedPos);
        }
    }

    private void loadRevenueStats() {
        swipeRefreshThongKe.setRefreshing(true);
        ApiClient.getApiService().getRevenueStats().enqueue(new Callback<ApiResponse<RevenueStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<RevenueStats>> call, Response<ApiResponse<RevenueStats>> response) {
                swipeRefreshThongKe.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    RevenueStats stats = response.body().getData();
                    DecimalFormat df = new DecimalFormat("#,###");
                    tvTongDoanhThu.setText(df.format(stats.getTotalRevenue()) + " VNĐ");
                    tvTongDon.setText(String.valueOf(stats.getTotalOrders()));
                    tvDaThanhToan.setText(String.valueOf(stats.getPaidOrders()));
                    tvDangNo.setText(String.valueOf(stats.getDebtOrders()));
                    tvChuaThanhToan.setText(String.valueOf(stats.getUnpaidOrders()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RevenueStats>> call, Throwable t) {
                swipeRefreshThongKe.setRefreshing(false);
            }
        });
    }

    private void loadDonHangs(String search) {
        ApiClient.getApiService().getDonHangs(null, null, null).enqueue(new Callback<ApiResponse<List<DonHang>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DonHang>>> call, Response<ApiResponse<List<DonHang>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    rawOrderList = response.body().getData();
                } else {
                    rawOrderList = new ArrayList<>();
                }
                applyFilterAndDisplay();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DonHang>>> call, Throwable t) {
                rawOrderList = new ArrayList<>();
                applyFilterAndDisplay();
            }
        });
    }

    private void loadDonHangsByMonth(int thang) {
        ApiClient.getApiService().getDonHangByThang(thang).enqueue(new Callback<ApiResponse<List<DonHang>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DonHang>>> call, Response<ApiResponse<List<DonHang>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    rawOrderList = response.body().getData();
                } else {
                    rawOrderList = new ArrayList<>();
                }
                applyFilterAndDisplay();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DonHang>>> call, Throwable t) {
                rawOrderList = new ArrayList<>();
                applyFilterAndDisplay();
            }
        });
    }
}

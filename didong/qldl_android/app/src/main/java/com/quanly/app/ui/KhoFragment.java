package com.quanly.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.quanly.app.R;
import com.quanly.app.adapter.KhoAdapter;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.Kho;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KhoFragment extends Fragment {

    private RecyclerView rvKho;
    private SwipeRefreshLayout swipeRefreshKho;
    private ExtendedFloatingActionButton fabAddKho;
    private Button btnHeaderAddKho;
    private KhoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kho, container, false);
        rvKho = view.findViewById(R.id.rvKho);
        swipeRefreshKho = view.findViewById(R.id.swipeRefreshKho);
        fabAddKho = view.findViewById(R.id.fabAddKho);
        btnHeaderAddKho = view.findViewById(R.id.btnHeaderAddKho);

        rvKho.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new KhoAdapter(new KhoAdapter.OnKhoClickListener() {
            @Override
            public void onChiTiet(Kho kho) {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), ChiTietKhoActivity.class);
                    intent.putExtra("kho", kho);
                    startActivityForResult(intent, 201);
                }
            }

            @Override
            public void onSua(Kho kho) {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), EditKhoActivity.class);
                    intent.putExtra("kho", kho);
                    startActivityForResult(intent, 202);
                }
            }
        });
        rvKho.setAdapter(adapter);

        if (swipeRefreshKho != null) {
            swipeRefreshKho.setOnRefreshListener(this::loadKho);
        }

        // Bắt sự kiện bấm nút Thêm Kho từ FAB
        if (fabAddKho != null) {
            fabAddKho.setOnClickListener(v -> {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), EditKhoActivity.class);
                    startActivityForResult(intent, 203);
                }
            });
        }

        // Bắt sự kiện bấm nút Thêm Kho từ Header Bar
        if (btnHeaderAddKho != null) {
            btnHeaderAddKho.setOnClickListener(v -> {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), EditKhoActivity.class);
                    startActivityForResult(intent, 203);
                }
            });
        }

        loadKho();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadKho();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadKho();
    }

    private void loadKho() {
        if (swipeRefreshKho != null) {
            swipeRefreshKho.setRefreshing(true);
        }
        ApiClient.getService().getKhos().enqueue(new Callback<ApiResponse<List<Kho>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Kho>>> call, Response<ApiResponse<List<Kho>>> response) {
                if (swipeRefreshKho != null) {
                    swipeRefreshKho.setRefreshing(false);
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Kho> list = response.body().getData();
                    adapter.setList(list);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Kho>>> call, Throwable t) {
                if (swipeRefreshKho != null) {
                    swipeRefreshKho.setRefreshing(false);
                }
            }
        });
    }
}

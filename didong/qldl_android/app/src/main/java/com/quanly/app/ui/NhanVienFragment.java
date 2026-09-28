package com.quanly.app.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.quanly.app.R;
import com.quanly.app.adapter.NhanVienAdapter;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.ChucVu;
import com.quanly.app.model.NhanVien;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NhanVienFragment extends Fragment {

    private EditText etSearchNV;
    private SwipeRefreshLayout swipeRefreshNV;
    private RecyclerView rvNhanVien;
    private FloatingActionButton fabThemNV;

    private NhanVienAdapter adapter;
    private List<ChucVu> listChucVu = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nhan_vien, container, false);

        etSearchNV = v.findViewById(R.id.etSearchNV);
        swipeRefreshNV = v.findViewById(R.id.swipeRefreshNV);
        rvNhanVien = v.findViewById(R.id.rvNhanVien);
        fabThemNV = v.findViewById(R.id.fabThemNV);

        rvNhanVien.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NhanVienAdapter(new NhanVienAdapter.OnNhanVienActionListener() {
            @Override
            public void onEdit(NhanVien nv) {
                showNhanVienDialog(nv);
            }

            @Override
            public void onDelete(NhanVien nv) {
                confirmDelete(nv);
            }
        });
        rvNhanVien.setAdapter(adapter);

        swipeRefreshNV.setOnRefreshListener(this::loadData);

        etSearchNV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadNhanVien(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabThemNV.setOnClickListener(view -> showNhanVienDialog(null));

        loadChucVu();
        loadData();

        return v;
    }

    private void loadData() {
        loadNhanVien(etSearchNV.getText().toString());
    }

    private void loadChucVu() {
        ApiClient.getApiService().getChucVus().enqueue(new Callback<ApiResponse<List<ChucVu>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChucVu>>> call, Response<ApiResponse<List<ChucVu>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listChucVu = response.body().getData();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChucVu>>> call, Throwable t) {}
        });
    }

    private void loadNhanVien(String search) {
        swipeRefreshNV.setRefreshing(true);
        ApiClient.getApiService().getNhanViens(search).enqueue(new Callback<ApiResponse<List<NhanVien>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<NhanVien>>> call, Response<ApiResponse<List<NhanVien>>> response) {
                swipeRefreshNV.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    adapter.setList(response.body().getData());
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách nhân viên", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<NhanVien>>> call, Throwable t) {
                swipeRefreshNV.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNhanVienDialog(@Nullable NhanVien nvEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_nhan_vien, null);
        builder.setView(view);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etTenNV = view.findViewById(R.id.etTenNV);
        Spinner spnChucVu = view.findViewById(R.id.spnChucVu);
        EditText etSDT = view.findViewById(R.id.etSDT);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);

        List<String> listRoles = new ArrayList<>();
        if (listChucVu.isEmpty()) {
            listRoles.add("QTHT - Quản trị hệ thống");
            listRoles.add("NVKD - Nhân viên Kinh doanh");
            listRoles.add("NVKT - Nhân viên Kế toán");
            listRoles.add("NVK - Nhân viên Kho");
            listRoles.add("NVGH - Nhân viên Giao hàng");
        } else {
            for (ChucVu cv : listChucVu) {
                listRoles.add(cv.getMaChucVu() + " - " + cv.getTenChucVu());
            }
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listRoles);
        spnChucVu.setAdapter(spinnerAdapter);

        if (nvEdit != null) {
            tvTitle.setText("Chỉnh Sửa Nhân Viên");
            etTenNV.setText(nvEdit.getTenNV());
            etSDT.setText(nvEdit.getSdt());
            etEmail.setText(nvEdit.getEmail());
            etUsername.setText(nvEdit.getUserName());
            etPassword.setText(nvEdit.getPassword());
        }

        builder.setPositiveButton(nvEdit == null ? "Thêm mới" : "Lưu thay đổi", (dialog, which) -> {
            String tenNV = etTenNV.getText().toString().trim();
            if (tenNV.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập họ tên nhân viên", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedRole = (String) spnChucVu.getSelectedItem();
            String maChucVu = selectedRole != null ? selectedRole.split(" - ")[0] : "NVKD";

            NhanVien nv = nvEdit != null ? nvEdit : new NhanVien();
            nv.setTenNV(tenNV);
            nv.setMaChucVu(maChucVu);
            nv.setSdt(etSDT.getText().toString().trim());
            nv.setEmail(etEmail.getText().toString().trim());
            nv.setUserName(etUsername.getText().toString().trim());
            nv.setPassword(etPassword.getText().toString().trim());

            if (nvEdit == null) {
                ApiClient.getApiService().createNhanVien(nv).enqueue(new Callback<ApiResponse<NhanVien>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<NhanVien>> call, Response<ApiResponse<NhanVien>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Thêm nhân viên thành công!", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            Toast.makeText(getContext(), "Thêm thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<NhanVien>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                ApiClient.getApiService().updateNhanVien(nvEdit.getMaNV(), nv).enqueue(new Callback<ApiResponse<NhanVien>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<NhanVien>> call, Response<ApiResponse<NhanVien>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Cập nhật nhân viên thành công!", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<NhanVien>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.create().show();
    }

    private void confirmDelete(NhanVien nv) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa nhân viên " + nv.getTenNV() + " (" + nv.getMaNV() + ")?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiClient.getApiService().deleteNhanVien(nv.getMaNV()).enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Xóa nhân viên thành công", Toast.LENGTH_SHORT).show();
                                loadData();
                            } else {
                                Toast.makeText(getContext(), "Không thể xóa nhân viên này", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

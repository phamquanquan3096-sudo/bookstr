package com.quanly.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.quanly.app.R;
import com.quanly.app.adapter.SanPhamAdapter;
import com.quanly.app.api.ApiClient;
import com.quanly.app.model.ApiResponse;
import com.quanly.app.model.SanPham;
import com.quanly.app.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SanPhamFragment extends Fragment implements CartManager.OnCartChangeListener {

    private RecyclerView rvSanPham;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private TextView tvCartBadge;
    private FloatingActionButton fabAddProduct;
    private SanPhamAdapter adapter;
    private String userId, userType;

    // Bộ lọc thể loại sách
    private LinearLayout layoutActiveFilter;
    private TextView tvActiveCategoryName, btnClearFilter;
    private String activeCategoryName = null;
    private String activeCategoryKeywords = null;

    public static SanPhamFragment newInstance(String userId, String userType) {
        SanPhamFragment fragment = new SanPhamFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("userType", userType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_san_pham, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            userType = getArguments().getString("userType");
        }
        if (userId == null || userId.isEmpty()) {
            userId = com.quanly.app.utils.SessionManager.getUserId(getContext());
        }
        if (userType == null || userType.isEmpty()) {
            userType = com.quanly.app.utils.SessionManager.getUserType(getContext());
        }
        if (userType == null || userType.isEmpty()) {
            userType = "ADMIN";
        }

        rvSanPham = view.findViewById(R.id.rvSanPham);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        etSearch = view.findViewById(R.id.etSearch);
        tvCartBadge = view.findViewById(R.id.tvCartBadge);
        fabAddProduct = view.findViewById(R.id.fabAddProduct);
        layoutActiveFilter = view.findViewById(R.id.layoutActiveFilter);
        tvActiveCategoryName = view.findViewById(R.id.tvActiveCategoryName);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);

        boolean canEditProduct = userType != null && (userType.contains("QTHT") || userType.contains("NVKD") || userType.equalsIgnoreCase("ADMIN"));

        long[] lastClickTime = new long[]{0};
        if (fabAddProduct != null) {
            if (canEditProduct) {
                fabAddProduct.setVisibility(View.VISIBLE);
                fabAddProduct.setOnClickListener(v -> {
                    if (System.currentTimeMillis() - lastClickTime[0] < 800) return;
                    lastClickTime[0] = System.currentTimeMillis();
                    Intent intent = new Intent(getContext(), EditSanPhamActivity.class);
                    startActivityForResult(intent, 101);
                });
            } else {
                fabAddProduct.setVisibility(View.GONE);
            }
        }

        View btnCart = view.findViewById(R.id.btnCart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                if (System.currentTimeMillis() - lastClickTime[0] < 800) return;
                lastClickTime[0] = System.currentTimeMillis();
                Intent intent = new Intent(getContext(), GioHangActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            });
        }

        // Nút Danh Mục Sản Phẩm (Mở quản lý thể loại sách đang bán)
        View btnDanhMuc = view.findViewById(R.id.btnDanhMuc);
        if (btnDanhMuc != null) {
            btnDanhMuc.setOnClickListener(v -> {
                if (System.currentTimeMillis() - lastClickTime[0] < 800) return;
                lastClickTime[0] = System.currentTimeMillis();
                Intent intent = new Intent(getContext(), DanhMucSachActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userType", userType);
                startActivityForResult(intent, 200);
            });
        }

        if (btnClearFilter != null) {
            btnClearFilter.setOnClickListener(v -> {
                activeCategoryName = null;
                activeCategoryKeywords = null;
                if (layoutActiveFilter != null) layoutActiveFilter.setVisibility(View.GONE);
                loadData();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã xóa bộ lọc thể loại!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        rvSanPham.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new SanPhamAdapter(sp -> {
            if (System.currentTimeMillis() - lastClickTime[0] < 800) return;
            lastClickTime[0] = System.currentTimeMillis();
            // Mở màn hình Chi Tiết Sản Phẩm khi nhấp vào bất kỳ sản phẩm nào
            Intent intent = new Intent(getContext(), ChiTietSanPhamActivity.class);
            intent.putExtra("sanPham", sp);
            intent.putExtra("userId", userId);
            intent.putExtra("userType", userType);
            startActivityForResult(intent, 100);
        });
        rvSanPham.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> {
            if (activeCategoryName != null) {
                filterProductsByCategory(activeCategoryName, activeCategoryKeywords);
            } else {
                loadData();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDataWithQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        CartManager.getInstance().addListener(this);
        updateCartBadge(CartManager.getInstance().getTotalCount());

        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activeCategoryName == null) {
            loadData();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            String catName = data.getStringExtra(DanhMucSachActivity.EXTRA_SELECTED_CATEGORY_NAME);
            String catKeywords = data.getStringExtra(DanhMucSachActivity.EXTRA_SELECTED_CATEGORY_KEYWORDS);
            if (catName != null && !catName.isEmpty()) {
                activeCategoryName = catName;
                activeCategoryKeywords = catKeywords;
                if (layoutActiveFilter != null) {
                    layoutActiveFilter.setVisibility(View.VISIBLE);
                    if (tvActiveCategoryName != null) {
                        tvActiveCategoryName.setText("Đang lọc thể loại: " + activeCategoryName);
                    }
                }
                filterProductsByCategory(activeCategoryName, activeCategoryKeywords);
                return;
            }
        }
        if (activeCategoryName != null) {
            filterProductsByCategory(activeCategoryName, activeCategoryKeywords);
        } else {
            loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CartManager.getInstance().removeListener(this);
    }

    private void updateCartBadge(int count) {
        if (tvCartBadge != null) {
            tvCartBadge.setText(String.valueOf(count));
        }
    }

    @Override
    public void onCartChanged(int totalCount, double totalAmount) {
        updateCartBadge(totalCount);
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService().getSanPhams(null).enqueue(new Callback<ApiResponse<List<SanPham>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SanPham>>> call, Response<ApiResponse<List<SanPham>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    adapter.setList(response.body().getData());
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể tải danh sách sản phẩm!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SanPham>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterProductsByCategory(String categoryName, String keywords) {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService().getSanPhams(null).enqueue(new Callback<ApiResponse<List<SanPham>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SanPham>>> call, Response<ApiResponse<List<SanPham>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<SanPham> allProducts = response.body().getData();
                    if (allProducts == null || allProducts.isEmpty()) {
                        adapter.setList(new ArrayList<>());
                        return;
                    }
                    String[] kwList = (keywords != null && !keywords.trim().isEmpty())
                            ? keywords.toLowerCase().split(",")
                            : new String[]{categoryName.toLowerCase()};

                    List<SanPham> filtered = new ArrayList<>();
                    for (SanPham sp : allProducts) {
                        String name = sp.getTenSP() != null ? sp.getTenSP().toLowerCase() : "";
                        String code = sp.getMaSP() != null ? sp.getMaSP().toLowerCase() : "";
                        boolean matched = false;
                        for (String kw : kwList) {
                            String trimmedKw = kw.trim();
                            if (!trimmedKw.isEmpty() && (name.contains(trimmedKw) || code.contains(trimmedKw))) {
                                matched = true;
                                break;
                            }
                        }
                        if (matched) {
                            filtered.add(sp);
                        }
                    }

                    if (filtered.isEmpty()) {
                        adapter.setList(allProducts);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Hiển thị toàn bộ sách (Chưa có sách riêng cho thể loại này)", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        adapter.setList(filtered);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Đã lọc " + filtered.size() + " sách thể loại \"" + categoryName + "\"", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SanPham>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadDataWithQuery(String query) {
        ApiClient.getService().getSanPhams(query).enqueue(new Callback<ApiResponse<List<SanPham>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SanPham>>> call, Response<ApiResponse<List<SanPham>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    adapter.setList(response.body().getData());
                }
            }
            @Override public void onFailure(Call<ApiResponse<List<SanPham>>> call, Throwable t) {}
        });
    }
}


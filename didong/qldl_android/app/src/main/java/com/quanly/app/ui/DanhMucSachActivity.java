package com.quanly.app.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quanly.app.R;
import com.quanly.app.adapter.TheLoaiSachAdapter;
import com.quanly.app.model.TheLoaiSach;
import com.quanly.app.utils.SessionManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DanhMucSachActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_CATEGORY_NAME = "selectedCategoryName";
    public static final String EXTRA_SELECTED_CATEGORY_KEYWORDS = "selectedCategoryKeywords";
    public static final String EXTRA_SELECTED_CATEGORY_CODE = "selectedCategoryCode";

    private static final String PREF_NAME = "BookCategoriesPref";
    private static final String KEY_CATEGORIES_JSON = "categories_list_json";

    private ImageView btnBack, btnClearSearch;
    private EditText etSearchCategory;
    private TextView tvHeaderSub, tvTotalCategoryBadge;
    private SwipeRefreshLayout swipeRefreshCategories;
    private RecyclerView rvTheLoaiSach;
    private FloatingActionButton fabAddCategory;
    
    private TheLoaiSachAdapter adapter;
    private final List<TheLoaiSach> categoryList = new ArrayList<>();
    private final Gson gson = new Gson();
    
    private String userId, userType;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_danh_muc_sach);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userId = getIntent().getStringExtra("userId");
        userType = getIntent().getStringExtra("userType");
        if (userId == null || userId.isEmpty()) {
            userId = SessionManager.getUserId(this);
        }
        if (userType == null || userType.isEmpty()) {
            userType = SessionManager.getUserType(this);
        }

        String role = (userType != null) ? userType.trim().toUpperCase() : "";
        isAdmin = role.contains("QTHT") || role.contains("ADMIN") || role.contains("NVKD") || role.contains("QUAN_TRI");

        initViews();
        loadCategories();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        etSearchCategory = findViewById(R.id.etSearchCategory);
        tvHeaderSub = findViewById(R.id.tvHeaderSub);
        tvTotalCategoryBadge = findViewById(R.id.tvTotalCategoryBadge);
        swipeRefreshCategories = findViewById(R.id.swipeRefreshCategories);
        rvTheLoaiSach = findViewById(R.id.rvTheLoaiSach);
        fabAddCategory = findViewById(R.id.fabAddCategory);

        rvTheLoaiSach.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TheLoaiSachAdapter(new TheLoaiSachAdapter.OnCategoryActionListener() {
            @Override
            public void onSelectCategory(TheLoaiSach category) {
                // Trả về thể loại được chọn để lọc trên màn hình sản phẩm
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_CATEGORY_NAME, category.getTenLoai());
                resultIntent.putExtra(EXTRA_SELECTED_CATEGORY_KEYWORDS, category.getKeywords());
                resultIntent.putExtra(EXTRA_SELECTED_CATEGORY_CODE, category.getMaLoai());
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onEditCategory(TheLoaiSach category) {
                showAddOrEditCategoryDialog(category);
            }

            @Override
            public void onDeleteCategory(TheLoaiSach category) {
                confirmDeleteCategory(category);
            }
        }, isAdmin);

        rvTheLoaiSach.setAdapter(adapter);

        if (isAdmin) {
            fabAddCategory.setVisibility(View.VISIBLE);
        } else {
            fabAddCategory.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearSearch.setOnClickListener(v -> {
            etSearchCategory.setText("");
            adapter.filter("");
            btnClearSearch.setVisibility(View.GONE);
        });

        etSearchCategory.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                adapter.filter(query);
                btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        swipeRefreshCategories.setOnRefreshListener(() -> {
            loadCategories();
            swipeRefreshCategories.setRefreshing(false);
            Toast.makeText(this, "🔄 Đã làm mới danh mục sách!", Toast.LENGTH_SHORT).show();
        });

        fabAddCategory.setOnClickListener(v -> showAddOrEditCategoryDialog(null));
    }

    private void loadCategories() {
        categoryList.clear();
        SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_CATEGORIES_JSON, null);

        if (json != null && !json.trim().isEmpty()) {
            try {
                Type listType = new TypeToken<List<TheLoaiSach>>() {}.getType();
                List<TheLoaiSach> saved = gson.fromJson(json, listType);
                if (saved != null && !saved.isEmpty()) {
                    categoryList.addAll(saved);
                }
            } catch (Exception ignored) {}
        }

        // Nếu chưa có dữ liệu lưu, nạp danh mục chuẩn đầy đủ của Book Shop
        if (categoryList.isEmpty()) {
            categoryList.addAll(getDefaultCategories());
            saveCategoriesToPref();
        }

        adapter.setList(categoryList);
        updateCategoryCountBadge();
    }

    private void saveCategoriesToPref() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(categoryList);
        pref.edit().putString(KEY_CATEGORIES_JSON, json).apply();
    }

    private void updateCategoryCountBadge() {
        if (tvTotalCategoryBadge != null) {
            tvTotalCategoryBadge.setText(categoryList.size() + " thể loại");
        }
    }

    private List<TheLoaiSach> getDefaultCategories() {
        List<TheLoaiSach> list = new ArrayList<>();
        list.add(new TheLoaiSach("TL01", "Văn Học & Tiểu Thuyết", "Tiểu thuyết kinh điển, tác phẩm văn học Việt Nam và quốc tế, trinh thám, lãng mạn.", 48, "#E91E63", "văn học, tiểu thuyết, truyện, tác phẩm, thơ, trinh thám, lãng mạn"));
        list.add(new TheLoaiSach("TL02", "Kinh Tế & Khởi Nghiệp", "Quản trị kinh doanh, tài chính cá nhân, đầu tư chứng khoán, marketing và bài học làm giàu.", 36, "#2E7D32", "kinh tế, khởi nghiệp, làm giàu, tài chính, đầu tư, kinh doanh, dạy con làm giàu, marketing"));
        list.add(new TheLoaiSach("TL03", "Kỹ Năng Sống & Phát Triển", "Rèn luyện tư duy, kỹ năng giao tiếp, nghệ thuật lãnh đạo, tâm lý học ứng dụng (Đắc Nhân Tâm...).", 52, "#EF6C00", "kỹ năng, phát triển bản thân, đắc nhân tâm, thói quen, tâm lý, tư duy, nhân tâm"));
        list.add(new TheLoaiSach("TL04", "Công Nghệ & Lập Trình", "Khoa học máy tính, lập trình Android/Web/AI, an ninh mạng, cơ sở dữ liệu và chuyển đổi số.", 28, "#1565C0", "công nghệ, lập trình, tin học, máy tính, ai, phần mềm, python, java, android"));
        list.add(new TheLoaiSach("TL05", "Truyện Tranh & Manga - Comic", "Truyện tranh Nhật Bản (Manga), Anime Artbook, Comic thiếu nhi (Doraemon, Conan, Naruto...).", 65, "#7B1FA2", "truyện tranh, manga, conan, naruto, doremon, comic, anime, thiếu nhi"));
        list.add(new TheLoaiSach("TL06", "Khoa Học & Đời Sống", "Khám phá vũ trụ, thiên văn học, y học thường thức, dinh dưỡng, sức khỏe và đời sống.", 24, "#00838F", "khoa học, đời sống, vũ trụ, sức khỏe, y học, tự nhiên, sinh học"));
        list.add(new TheLoaiSach("TL07", "Lịch Sử & Triết Học", "Lịch sử hào hùng dân tộc, sử thế giới, khảo cổ học, danh nhân và tư tưởng triết học các thời đại.", 20, "#5D4037", "lịch sử, triết học, danh nhân, khảo cổ, văn hóa, chiến tranh, việt nam"));
        list.add(new TheLoaiSach("TL08", "Ngoại Ngữ & Từ Điển", "Sách học và luyện thi chứng chỉ quốc tế tiếng Anh (IELTS, TOEIC), tiếng Nhật (JLPT), tiếng Hàn, tiếng Trung.", 30, "#283593", "ngoại ngữ, tiếng anh, ielts, toeic, tiếng nhật, tiếng hàn, từ điển, ngữ pháp"));
        list.add(new TheLoaiSach("TL09", "Sách Giáo Khoa & Tham Khảo", "Sách giáo khoa chuẩn Bộ Giáo dục, sách bài tập bồi dưỡng học sinh giỏi, bộ đề ôn thi THPT.", 42, "#00695C", "giáo khoa, tham khảo, toán, lý, hóa, văn, ôn thi, đại học, thpt"));
        return list;
    }

    private void showAddOrEditCategoryDialog(@Nullable TheLoaiSach categoryToEdit) {
        boolean isEdit = (categoryToEdit != null);
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_the_loai);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        EditText etMaLoai = dialog.findViewById(R.id.etDialogMaLoai);
        EditText etTenLoai = dialog.findViewById(R.id.etDialogTenLoai);
        EditText etMoTa = dialog.findViewById(R.id.etDialogMoTa);
        EditText etKeywords = dialog.findViewById(R.id.etDialogKeywords);
        Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnSave = dialog.findViewById(R.id.btnDialogSave);

        if (isEdit) {
            tvTitle.setText("Chỉnh Sửa Thể Loại Sách");
            etMaLoai.setText(categoryToEdit.getMaLoai());
            etMaLoai.setEnabled(false); // Mã không cho sửa
            etTenLoai.setText(categoryToEdit.getTenLoai());
            etMoTa.setText(categoryToEdit.getMoTa());
            etKeywords.setText(categoryToEdit.getKeywords());
            btnSave.setText("Cập Nhật");
        } else {
            tvTitle.setText("Thêm Thể Loại Sách Mới");
            etMaLoai.setText("TL" + String.format("%02d", categoryList.size() + 1));
            btnSave.setText("Thêm Mới");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String ma = etMaLoai.getText().toString().trim();
            String ten = etTenLoai.getText().toString().trim();
            String moTa = etMoTa.getText().toString().trim();
            String keywords = etKeywords.getText().toString().trim();

            if (ten.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên thể loại sách!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit) {
                categoryToEdit.setTenLoai(ten);
                categoryToEdit.setMoTa(moTa.isEmpty() ? "Thể loại sách trong cửa hàng." : moTa);
                categoryToEdit.setKeywords(keywords);
                Toast.makeText(this, "🎉 Đã cập nhật thể loại: " + ten, Toast.LENGTH_SHORT).show();
            } else {
                TheLoaiSach newItem = new TheLoaiSach(
                        ma.isEmpty() ? "TL" + (System.currentTimeMillis() % 1000) : ma,
                        ten,
                        moTa.isEmpty() ? "Thể loại sách trong cửa hàng." : moTa,
                        15,
                        "#1E88E5",
                        keywords.isEmpty() ? ten.toLowerCase() : keywords
                );
                categoryList.add(newItem);
                Toast.makeText(this, "🎉 Đã thêm thể loại sách mới: " + ten, Toast.LENGTH_SHORT).show();
            }

            saveCategoriesToPref();
            adapter.setList(categoryList);
            updateCategoryCountBadge();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void confirmDeleteCategory(TheLoaiSach category) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvName = dialog.findViewById(R.id.tvDialogProductName);
        TextView tvCode = dialog.findViewById(R.id.tvDialogProductCode);
        Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnDialogConfirm);

        if (tvName != null) tvName.setText(category.getTenLoai());
        if (tvCode != null) tvCode.setText("Mã: " + category.getMaLoai());

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                categoryList.remove(category);
                saveCategoriesToPref();
                adapter.setList(categoryList);
                updateCategoryCountBadge();
                Toast.makeText(this, "🗑️ Đã xóa thể loại: " + category.getTenLoai(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}

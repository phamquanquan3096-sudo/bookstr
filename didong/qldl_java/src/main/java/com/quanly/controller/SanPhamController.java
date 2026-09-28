package com.quanly.controller;

import com.quanly.dto.ApiResponse;
import com.quanly.entity.SanPham;
import com.quanly.repository.ChiTietDonHangRepository;
import com.quanly.repository.ChiTietKhoRepository;
import com.quanly.repository.SanPhamRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sanpham")
@CrossOrigin(origins = "*")
public class SanPhamController {

    private final SanPhamRepository sanPhamRepo;
    private final ChiTietKhoRepository chiTietKhoRepo;
    private final ChiTietDonHangRepository chiTietDonHangRepo;

    public SanPhamController(SanPhamRepository sanPhamRepo, ChiTietKhoRepository chiTietKhoRepo, ChiTietDonHangRepository chiTietDonHangRepo) {
        this.sanPhamRepo = sanPhamRepo;
        this.chiTietKhoRepo = chiTietKhoRepo;
        this.chiTietDonHangRepo = chiTietDonHangRepo;
    }

    @GetMapping
    public ApiResponse<List<SanPham>> getAll(@RequestParam(required = false) String query) {
        List<SanPham> list;
        if (query != null && !query.trim().isEmpty()) {
            list = sanPhamRepo.findByTenSPContainingIgnoreCaseOrMaSPContainingIgnoreCase(query, query);
        } else {
            list = sanPhamRepo.findAll();
        }

        for (SanPham sp : list) {
            String resolved = resolveHinhAnh(sp);
            if (!resolved.equals(sp.getHinhAnh())) {
                sp.setHinhAnh(resolved);
                try {
                    sanPhamRepo.save(sp);
                } catch (Exception ignored) {}
            }
        }
        return ApiResponse.ok("Lấy danh sách sản phẩm thành công", list);
    }

    @GetMapping("/{id}")
    public ApiResponse<SanPham> getById(@PathVariable String id) {
        return sanPhamRepo.findById(id)
                .map(sp -> {
                    sp.setHinhAnh(resolveHinhAnh(sp));
                    return ApiResponse.ok("Thành công", sp);
                })
                .orElseGet(() -> ApiResponse.error("Không tìm thấy sản phẩm với mã: " + id));
    }

    @PostMapping
    public ApiResponse<SanPham> save(@RequestBody SanPham sanPham) {
        if (sanPham.getMaSP() == null || sanPham.getMaSP().isEmpty()) {
            sanPham.setMaSP("SP" + System.currentTimeMillis() % 10000);
        }
        if (sanPham.getHinhAnh() == null || sanPham.getHinhAnh().trim().isEmpty() || sanPham.getHinhAnh().startsWith("content://")) {
            sanPham.setHinhAnh(resolveHinhAnh(sanPham));
        }
        SanPham saved = sanPhamRepo.save(sanPham);
        return ApiResponse.ok("Lưu sản phẩm thành công", saved);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<String> delete(@PathVariable String id) {
        try {
            chiTietKhoRepo.deleteByMaSP(id);
            chiTietDonHangRepo.deleteByMaSP(id);
            sanPhamRepo.deleteById(id);
            return ApiResponse.ok("Đã xóa sản phẩm", id);
        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }

    private String resolveHinhAnh(SanPham sp) {
        String hinhAnh = sp.getHinhAnh();
        if (hinhAnh != null && !hinhAnh.trim().isEmpty() && !hinhAnh.startsWith("/Data/") && !hinhAnh.startsWith("content://")) {
            return hinhAnh;
        }

        String name = sp.getTenSP() != null ? sp.getTenSP().toLowerCase() : "";
        if (name.contains("đắc nhân tâm") || name.contains("dac nhan tam") || name.contains("nhân tâm")) {
            return "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&q=80";
        } else if (name.contains("dạy con") || name.contains("làm giàu")) {
            return "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500&q=80";
        } else if (name.contains("naruto") || name.contains("truyện") || name.contains("manga")) {
            return "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500&q=80";
        } else if (name.contains("giả kim")) {
            return "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=500&q=80";
        } else if (name.contains("conan")) {
            return "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500&q=80";
        } else {
            return "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500&q=80";
        }
    }
}

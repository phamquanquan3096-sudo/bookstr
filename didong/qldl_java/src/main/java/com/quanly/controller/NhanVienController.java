package com.quanly.controller;

import com.quanly.dto.ApiResponse;
import com.quanly.entity.ChucVu;
import com.quanly.entity.NhanVien;
import com.quanly.repository.ChucVuRepository;
import com.quanly.repository.NhanVienRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/nhanvien")
@CrossOrigin(origins = "*")
public class NhanVienController {

    private final NhanVienRepository nhanVienRepo;
    private final ChucVuRepository chucVuRepo;

    public NhanVienController(NhanVienRepository nhanVienRepo, ChucVuRepository chucVuRepo) {
        this.nhanVienRepo = nhanVienRepo;
        this.chucVuRepo = chucVuRepo;
    }

    @GetMapping
    public ApiResponse<List<NhanVien>> getAllNhanVien(@RequestParam(required = false) String search) {
        List<NhanVien> list = nhanVienRepo.findAll();
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim().toLowerCase();
            list = list.stream()
                    .filter(nv -> (nv.getTenNV() != null && nv.getTenNV().toLowerCase().contains(query)) ||
                            (nv.getMaNV() != null && nv.getMaNV().toLowerCase().contains(query)) ||
                            (nv.getMaChucVu() != null && nv.getMaChucVu().toLowerCase().contains(query)))
                    .toList();
        }
        return ApiResponse.ok("Danh sách nhân viên", list);
    }

    @GetMapping("/chucvu")
    public ApiResponse<List<ChucVu>> getChucVus() {
        return ApiResponse.ok("Danh sách chức vụ", chucVuRepo.findAll());
    }

    @GetMapping("/{maNV}")
    public ApiResponse<NhanVien> getNhanVienByMa(@PathVariable String maNV) {
        Optional<NhanVien> nv = nhanVienRepo.findById(maNV);
        return nv.map(nhanVien -> ApiResponse.ok("Thông tin nhân viên", nhanVien))
                .orElseGet(() -> ApiResponse.error("Không tìm thấy nhân viên: " + maNV));
    }

    @PostMapping
    public ApiResponse<NhanVien> createNhanVien(@RequestBody NhanVien nv) {
        if (nv.getMaNV() == null || nv.getMaNV().trim().isEmpty()) {
            nv.setMaNV("NV" + (System.currentTimeMillis() % 10000));
        }
        if (nv.getPassword() == null || nv.getPassword().isEmpty()) {
            nv.setPassword("123456");
        }
        NhanVien saved = nhanVienRepo.save(nv);
        return ApiResponse.ok("Thêm nhân viên thành công", saved);
    }

    @PutMapping("/{maNV}")
    public ApiResponse<NhanVien> updateNhanVien(@PathVariable String maNV, @RequestBody NhanVien updated) {
        return nhanVienRepo.findById(maNV).map(nv -> {
            if (updated.getTenNV() != null) nv.setTenNV(updated.getTenNV());
            if (updated.getMaChucVu() != null) nv.setMaChucVu(updated.getMaChucVu());
            if (updated.getSdt() != null) nv.setSdt(updated.getSdt());
            if (updated.getEmail() != null) nv.setEmail(updated.getEmail());
            if (updated.getUserName() != null) nv.setUserName(updated.getUserName());
            if (updated.getPassword() != null && !updated.getPassword().isEmpty()) {
                nv.setPassword(updated.getPassword());
            }
            NhanVien saved = nhanVienRepo.save(nv);
            return ApiResponse.ok("Cập nhật thông tin nhân viên thành công", saved);
        }).orElseGet(() -> ApiResponse.error("Không tìm thấy nhân viên để cập nhật: " + maNV));
    }

    @DeleteMapping("/{maNV}")
    public ApiResponse<String> deleteNhanVien(@PathVariable String maNV) {
        if (nhanVienRepo.existsById(maNV)) {
            nhanVienRepo.deleteById(maNV);
            return ApiResponse.ok("Xóa nhân viên thành công", maNV);
        }
        return ApiResponse.error("Không tìm thấy nhân viên để xóa: " + maNV);
    }
}

package com.quanly.controller;

import com.quanly.dto.ApiResponse;
import com.quanly.entity.ChiTietKho;
import com.quanly.entity.Kho;
import com.quanly.repository.ChiTietKhoRepository;
import com.quanly.repository.KhoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kho")
@CrossOrigin(origins = "*")
public class KhoController {

    private final KhoRepository khoRepo;
    private final ChiTietKhoRepository chiTietKhoRepo;

    public KhoController(KhoRepository khoRepo, ChiTietKhoRepository chiTietKhoRepo) {
        this.khoRepo = khoRepo;
        this.chiTietKhoRepo = chiTietKhoRepo;
    }

    @GetMapping
    public ApiResponse<List<Kho>> getAll() {
        return ApiResponse.ok("Danh sách kho hàng", khoRepo.findAll());
    }

    @GetMapping("/{maKho}/chitiet")
    public ApiResponse<List<ChiTietKho>> getChiTietKho(@PathVariable String maKho) {
        return ApiResponse.ok("Chi tiết hàng trong kho " + maKho, chiTietKhoRepo.findByMaKho(maKho));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getKhoStats() {
        long tongKho = khoRepo.count();
        List<ChiTietKho> allChiTiet = chiTietKhoRepo.findAll();

        int tongSPSapHetHang = 0;
        int tongSPTonKho = 0;
        int tongSPHetHang = 0;

        for (ChiTietKho ct : allChiTiet) {
            if ("Sắp hết hàng".equalsIgnoreCase(ct.getTinhTrang())) {
                tongSPSapHetHang++;
            } else if ("Tồn kho".equalsIgnoreCase(ct.getTinhTrang())) {
                tongSPTonKho++;
            } else if ("Hết hàng".equalsIgnoreCase(ct.getTinhTrang())) {
                tongSPHetHang++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("tongKho", tongKho);
        stats.put("tongSPSapHetHang", tongSPSapHetHang);
        stats.put("tongSPTonKho", tongSPTonKho);
        stats.put("tongSPHetHang", tongSPHetHang);

        return ApiResponse.ok("Thống kê phòng kho thành công", stats);
    }

    @PostMapping
    public ApiResponse<Kho> createKho(@RequestBody Kho kho) {
        if (kho.getMaKho() == null || kho.getMaKho().trim().isEmpty()) {
            kho.setMaKho("KO" + (100 + new java.util.Random().nextInt(900)));
        }
        Kho saved = khoRepo.save(kho);
        return ApiResponse.ok("Thêm kho hàng mới thành công", saved);
    }

    @GetMapping("/{maKho}")
    public ApiResponse<Kho> getKhoByMa(@PathVariable String maKho) {
        return khoRepo.findById(maKho)
                .map(k -> ApiResponse.ok("Thông tin kho hàng", k))
                .orElseGet(() -> ApiResponse.error("Không tìm thấy kho hàng: " + maKho));
    }

    @PutMapping("/{maKho}")
    public ApiResponse<Kho> updateKho(@PathVariable String maKho, @RequestBody Kho kho) {
        if (!khoRepo.existsById(maKho)) {
            return ApiResponse.error("Không tìm thấy kho hàng: " + maKho);
        }
        kho.setMaKho(maKho);
        Kho updated = khoRepo.save(kho);
        return ApiResponse.ok("Cập nhật thông tin kho hàng thành công", updated);
    }

    @DeleteMapping("/{maKho}")
    public ApiResponse<String> deleteKho(@PathVariable String maKho) {
        if (!khoRepo.existsById(maKho)) {
            return ApiResponse.error("Không tìm thấy kho hàng: " + maKho);
        }
        khoRepo.deleteById(maKho);
        return ApiResponse.ok("Đã xóa kho hàng thành công", maKho);
    }

    @PostMapping("/chitiet/capnhat")
    public ApiResponse<ChiTietKho> updateChiTietKho(@RequestBody ChiTietKho chiTietKho) {
        ChiTietKho saved = chiTietKhoRepo.save(chiTietKho);
        return ApiResponse.ok("Cập nhật kho thành công", saved);
    }
}

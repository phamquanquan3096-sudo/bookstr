package com.quanly.controller;

import com.quanly.dto.ApiResponse;
import com.quanly.entity.DaiLy;
import com.quanly.entity.LoaiDL;
import com.quanly.repository.DaiLyRepository;
import com.quanly.repository.LoaiDLRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/daily")
@CrossOrigin(origins = "*")
public class DaiLyController {

    private final DaiLyRepository daiLyRepo;
    private final LoaiDLRepository loaiDLRepo;

    public DaiLyController(DaiLyRepository daiLyRepo, LoaiDLRepository loaiDLRepo) {
        this.daiLyRepo = daiLyRepo;
        this.loaiDLRepo = loaiDLRepo;
    }

    @GetMapping
    public ApiResponse<List<DaiLy>> getAll() {
        return ApiResponse.ok("Danh sách đại lý", daiLyRepo.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DaiLy> getById(@PathVariable String id) {
        return daiLyRepo.findById(id)
                .map(dl -> ApiResponse.ok("Thành công", dl))
                .orElseGet(() -> ApiResponse.error("Không tìm thấy đại lý với mã: " + id));
    }

    @PostMapping
    public ApiResponse<DaiLy> createOrUpdate(@RequestBody DaiLy daiLy) {
        DaiLy saved = daiLyRepo.save(daiLy);
        return ApiResponse.ok("Lưu thông tin đại lý thành công", saved);
    }

    @GetMapping("/loai")
    public ApiResponse<List<LoaiDL>> getAllLoaiDL() {
        return ApiResponse.ok("Danh sách loại đại lý", loaiDLRepo.findAll());
    }

    @GetMapping("/check-name")
    public ApiResponse<Boolean> checkTenDL(@RequestParam String tenDL) {
        boolean exists = daiLyRepo.findByTenDL(tenDL).isPresent();
        return ApiResponse.ok(exists ? "Tên đại lý đã tồn tại" : "Tên đại lý hợp lệ", !exists);
    }

    @GetMapping("/check-sdt")
    public ApiResponse<Boolean> checkSDT(@RequestParam String sdt) {
        boolean exists = daiLyRepo.findBySdt(sdt).isPresent();
        return ApiResponse.ok(exists ? "Số điện thoại đã được sử dụng" : "Số điện thoại hợp lệ", !exists);
    }

    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        if (email == null || !email.contains("@gmail.com")) {
            return ApiResponse.error("Email phải có định dạng @gmail.com");
        }
        boolean exists = daiLyRepo.findByEmail(email).isPresent();
        return ApiResponse.ok(exists ? "Email đã được sử dụng" : "Email hợp lệ", !exists);
    }
}

package com.quanly.controller;

import com.quanly.dto.ApiResponse;
import com.quanly.dto.LoginRequest;
import com.quanly.dto.LoginResponse;
import com.quanly.entity.DaiLy;
import com.quanly.entity.NhanVien;
import com.quanly.repository.DaiLyRepository;
import com.quanly.repository.NhanVienRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final NhanVienRepository nhanVienRepo;
    private final DaiLyRepository daiLyRepo;

    public AuthController(NhanVienRepository nhanVienRepo, DaiLyRepository daiLyRepo) {
        this.nhanVienRepo = nhanVienRepo;
        this.daiLyRepo = daiLyRepo;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // 1. First check NhanVien
        Optional<NhanVien> staff = nhanVienRepo.findByUserNameAndPassword(request.getUsername(), request.getPassword());
        if (staff.isPresent()) {
            NhanVien nv = staff.get();
            LoginResponse response = new LoginResponse(true, "Đăng nhập nhân viên thành công", nv.getMaChucVu(), nv.getMaNV(), nv.getTenNV());
            response.setToken("TOKEN_STAFF_" + nv.getMaNV());
            return response;
        }

        // 2. Check DaiLy (Khách hàng)
        Optional<DaiLy> agency = daiLyRepo.findByUserNameAndPassword(request.getUsername(), request.getPassword());
        if (agency.isPresent()) {
            DaiLy dl = agency.get();
            LoginResponse response = new LoginResponse(true, "Đăng nhập khách hàng thành công", "KH", dl.getMaDL(), dl.getTenDL());
            response.setToken("TOKEN_KH_" + dl.getMaDL());
            return response;
        }

        return new LoginResponse(false, "Mật khẩu hoặc tên đăng nhập không chính xác", null, null, null);
    }

    @PostMapping("/register-daily")
    public ApiResponse<DaiLy> registerDaily(@RequestBody DaiLy daiLy) {
        if (daiLyRepo.findByUserName(daiLy.getUserName()).isPresent()) {
            return ApiResponse.error("Tên đăng nhập đã tồn tại!");
        }
        if (daiLy.getSdt() != null && daiLyRepo.findBySdt(daiLy.getSdt()).isPresent()) {
            return ApiResponse.error("Số điện thoại đã được đăng ký!");
        }

        Random random = new Random();
        String maDL = "KH" + (1000 + random.nextInt(9000));
        daiLy.setMaDL(maDL);
        if (daiLy.getMaLoaiDL() == null) {
            daiLy.setMaLoaiDL("LDL03");
        }
        daiLy.setNgayTao(new Date());
        daiLy.setTongDoanhSo(0.0);

        DaiLy saved = daiLyRepo.save(daiLy);
        return ApiResponse.ok("Đăng ký tài khoản khách hàng thành công", saved);
    }
}

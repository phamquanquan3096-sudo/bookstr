package com.quanly.controller;

import com.quanly.dto.ApiResponse;
import com.quanly.entity.DonHang;
import com.quanly.repository.DonHangRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/giaohang")
@CrossOrigin(origins = "*")
public class GiaoHangController {

    private final DonHangRepository donHangRepo;

    public GiaoHangController(DonHangRepository donHangRepo) {
        this.donHangRepo = donHangRepo;
    }

    @GetMapping("/donhang/{maGH}")
    public ApiResponse<List<DonHang>> getAssignedOrders(@PathVariable String maGH) {
        return ApiResponse.ok("Danh sách đơn hàng cần giao", donHangRepo.findByMaGH(maGH));
    }

    @PutMapping("/donhang/{id}/capnhat")
    public ApiResponse<DonHang> updateDeliveryStatus(@PathVariable String id,
                                                      @RequestParam String tinhTrangGH,
                                                      @RequestParam(required = false) String trangThaiThanhToan) {
        return donHangRepo.findById(id).map(dh -> {
            dh.setTinhTrangGH(tinhTrangGH);
            if (trangThaiThanhToan != null) {
                dh.setTinhTrangThanhToan(trangThaiThanhToan);
            }
            if ("Đã giao".equalsIgnoreCase(tinhTrangGH)) {
                dh.setTrangThai("Hoàn thành");
            }
            return ApiResponse.ok("Cập nhật giao hàng thành công", donHangRepo.save(dh));
        }).orElseGet(() -> ApiResponse.error("Không tìm thấy đơn hàng: " + id));
    }
}

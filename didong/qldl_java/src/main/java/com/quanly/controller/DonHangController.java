package com.quanly.controller;

import com.quanly.dto.ApiResponse;
import com.quanly.dto.OrderCreateRequest;
import com.quanly.dto.OrderItemDto;
import com.quanly.entity.ChiTietDonHang;
import com.quanly.entity.DonHang;
import com.quanly.entity.SanPham;
import com.quanly.repository.ChiTietDonHangRepository;
import com.quanly.repository.DonHangRepository;
import com.quanly.repository.SanPhamRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/donhang")
@CrossOrigin(origins = "*")
public class DonHangController {

    private final DonHangRepository donHangRepo;
    private final ChiTietDonHangRepository chiTietRepo;
    private final SanPhamRepository sanPhamRepo;

    public DonHangController(DonHangRepository donHangRepo, ChiTietDonHangRepository chiTietRepo, SanPhamRepository sanPhamRepo) {
        this.donHangRepo = donHangRepo;
        this.chiTietRepo = chiTietRepo;
        this.sanPhamRepo = sanPhamRepo;
    }

    @GetMapping
    public ApiResponse<List<DonHang>> getAll(@RequestParam(required = false) String maDL,
                                            @RequestParam(required = false) String maGH,
                                            @RequestParam(required = false) String trangThai) {
        if (maDL != null) {
            return ApiResponse.ok("Đơn hàng đại lý", donHangRepo.findByMaDL(maDL));
        }
        if (maGH != null) {
            return ApiResponse.ok("Đơn hàng giao bởi NVGH", donHangRepo.findByMaGH(maGH));
        }
        if (trangThai != null) {
            return ApiResponse.ok("Đơn hàng theo trạng thái", donHangRepo.findByTrangThai(trangThai));
        }
        return ApiResponse.ok("Danh sách tất cả đơn hàng", donHangRepo.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getDetail(@PathVariable String id) {
        try {
            String searchId = id != null ? id.trim() : "";
            Optional<DonHang> donHangOpt = donHangRepo.findById(searchId);
            DonHang donHang = donHangOpt.orElse(null);

            List<ChiTietDonHang> items = new ArrayList<>();
            try {
                items = chiTietRepo.findByMaDH(searchId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (items != null) {
                for (ChiTietDonHang item : items) {
                    if (item.getMaSP() != null) {
                        try {
                            sanPhamRepo.findById(item.getMaSP().trim()).ifPresent(sp -> {
                                item.setTenSP(sp.getTenSP());
                                if (item.getDonGia() == null) {
                                    item.setDonGia(sp.getGia() != null ? sp.getGia().doubleValue() : 0.0);
                                }
                            });
                        } catch (Exception ignored) {}
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("donHang", donHang);
            result.put("chiTiet", items != null ? items : new ArrayList<>());

            return ApiResponse.ok("Chi tiết đơn hàng", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi tải chi tiết đơn hàng: " + e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<DonHang> createOrder(@RequestBody OrderCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ApiResponse.error("Đơn hàng không có sản phẩm!");
        }

        try {
            String maDH = "DH" + (System.currentTimeMillis() % 100000);
            DonHang dh = new DonHang();
            dh.setMaDH(maDH);
            
            String maDL = request.getMaDL();
            if (maDL == null || maDL.trim().isEmpty() || maDL.length() > 10) {
                maDL = "DL01";
            }
            dh.setMaDL(maDL);

            dh.setNgayLap(new Date());
            dh.setTrangThai("Mới tạo");
            dh.setTinhTrangThanhToan("Chưa thanh toán");
            dh.setDiemGiao(request.getDiemGiao());
            dh.setTinhTrangGH("Chờ phân công");
            dh.setPhieuXuatKho(false);
            dh.setXuatHoaDon(false);

            dh.setTongTien(0.0);
            DonHang savedDH = donHangRepo.save(dh);

            double tongTien = 0.0;

            for (OrderItemDto item : request.getItems()) {
                Optional<SanPham> spOpt = sanPhamRepo.findById(item.getMaSP());
                if (spOpt.isPresent()) {
                    SanPham sp = spOpt.get();
                    double donGia = sp.getGia() != null ? sp.getGia().doubleValue() : 0.0;
                    double thanhTien = donGia * item.getSoLuong();
                    tongTien += thanhTien;

                    ChiTietDonHang ct = new ChiTietDonHang(maDH, item.getMaSP(), item.getSoLuong(), donGia, thanhTien);
                    chiTietRepo.save(ct);

                    // Update inventory
                    if (sp.getTongTon() != null && sp.getTongTon() >= item.getSoLuong()) {
                        sp.setTongTon(sp.getTongTon() - item.getSoLuong());
                        sanPhamRepo.save(sp);
                    }
                }
            }

            savedDH.setTongTien(tongTien);
            donHangRepo.save(savedDH);

            return ApiResponse.ok("Tạo đơn hàng thành công", savedDH);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi tạo đơn hàng: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/trangthai")
    public ApiResponse<DonHang> updateStatus(@PathVariable String id, @RequestParam(required = false) String trangThai) {
        String searchId = id != null ? id.trim() : "";
        String status = (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : "Đã hủy";
        return donHangRepo.findById(searchId).map(dh -> {
            dh.setTrangThai(status);
            return ApiResponse.ok("Cập nhật trạng thái thành công", donHangRepo.save(dh));
        }).orElseGet(() -> ApiResponse.error("Không tìm thấy đơn hàng: " + searchId));
    }

    @PutMapping("/{id}/huy")
    public ApiResponse<DonHang> cancelOrder(@PathVariable String id) {
        String searchId = id != null ? id.trim() : "";
        return donHangRepo.findById(searchId).map(dh -> {
            dh.setTrangThai("Đã hủy");
            return ApiResponse.ok("Hủy đơn hàng thành công", donHangRepo.save(dh));
        }).orElseGet(() -> ApiResponse.error("Không tìm thấy đơn hàng: " + searchId));
    }

    @PutMapping("/{id}/phancong-giaohang")
    public ApiResponse<DonHang> assignDelivery(@PathVariable String id, @RequestParam String maGH) {
        String searchId = id != null ? id.trim() : "";
        return donHangRepo.findById(searchId).map(dh -> {
            dh.setMaGH(maGH);
            dh.setTinhTrangGH("Đang giao");
            return ApiResponse.ok("Phân công giao hàng thành công", donHangRepo.save(dh));
        }).orElseGet(() -> ApiResponse.error("Không tìm thấy đơn hàng: " + searchId));
    }

    @PutMapping("/{id}/phe-duyet")
    public ApiResponse<DonHang> approveOrder(@PathVariable String id) {
        String searchId = id != null ? id.trim() : "";
        return donHangRepo.findById(searchId).map(dh -> {
            dh.setTrangThai("Đã duyệt");
            dh.setTinhTrangGH("Đang giao");
            return ApiResponse.ok("Phê duyệt đơn hàng thành công", donHangRepo.save(dh));
        }).orElseGet(() -> ApiResponse.error("Không tìm thấy đơn hàng: " + searchId));
    }

    @PutMapping("/{id}/tu-choi")
    public ApiResponse<DonHang> rejectOrder(@PathVariable String id, @RequestParam(required = false) String lyDo) {
        String searchId = id != null ? id.trim() : "";
        return donHangRepo.findById(searchId).map(dh -> {
            dh.setTrangThai("Từ chối");
            return ApiResponse.ok("Từ chối đơn hàng thành công" + (lyDo != null ? ": " + lyDo : ""), donHangRepo.save(dh));
        }).orElseGet(() -> ApiResponse.error("Không tìm thấy đơn hàng: " + searchId));
    }

    @PutMapping("/{id}/giao-hang")
    public ApiResponse<DonHang> updateDeliveryDone(@PathVariable String id) {
        String searchId = id != null ? id.trim() : "";
        return donHangRepo.findById(searchId).map(dh -> {
            dh.setTinhTrangGH("Đã giao");
            dh.setTrangThai("Đã giao");
            return ApiResponse.ok("Xác nhận đã giao hàng thành công", donHangRepo.save(dh));
        }).orElseGet(() -> ApiResponse.error("Không tìm thấy đơn hàng: " + searchId));
    }

    @PutMapping("/{id}/thanh-toan")
    public ApiResponse<DonHang> updatePaymentDone(@PathVariable String id) {
        String searchId = id != null ? id.trim() : "";
        return donHangRepo.findById(searchId).map(dh -> {
            dh.setTinhTrangThanhToan("Đã thanh toán");
            return ApiResponse.ok("Xác nhận thanh toán thành công", donHangRepo.save(dh));
        }).orElseGet(() -> ApiResponse.error("Không tìm thấy đơn hàng: " + searchId));
    }
}

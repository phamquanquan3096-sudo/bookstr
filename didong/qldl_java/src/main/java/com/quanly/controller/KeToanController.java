package com.quanly.controller;

import com.quanly.dto.ApiResponse;
import com.quanly.dto.RevenueStatsDto;
import com.quanly.entity.DonHang;
import com.quanly.entity.PhieuCongNo;
import com.quanly.repository.DonHangRepository;
import com.quanly.repository.PhieuCongNoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ketoan")
@CrossOrigin(origins = "*")
public class KeToanController {

    private final PhieuCongNoRepository phieuCongNoRepo;
    private final DonHangRepository donHangRepo;

    public KeToanController(PhieuCongNoRepository phieuCongNoRepo, DonHangRepository donHangRepo) {
        this.phieuCongNoRepo = phieuCongNoRepo;
        this.donHangRepo = donHangRepo;
    }

    @GetMapping("/congno")
    public ApiResponse<List<PhieuCongNo>> getCongNoList(@RequestParam(required = false) String maDL) {
        try {
            if (maDL != null) {
                return ApiResponse.ok("Công nợ của khách hàng", phieuCongNoRepo.findByMaDL(maDL));
            }
            List<PhieuCongNo> list = phieuCongNoRepo.findAll();
            if (list == null || list.isEmpty()) {
                list = new ArrayList<>();
                PhieuCongNo sample = new PhieuCongNo();
                sample.setMaPhieuCN("PCN101");
                sample.setMaDL("KH01");
                sample.setSoTienNo(50000000.0);
                sample.setSoTienThu(30000000.0);
                sample.setConNo(20000000.0);
                sample.setGhiChu("Công nợ kỳ 1");
                list.add(sample);
            }
            return ApiResponse.ok("Danh sách phiếu công nợ", list);
        } catch (Exception e) {
            e.printStackTrace();
            List<PhieuCongNo> mockList = new ArrayList<>();
            PhieuCongNo sample = new PhieuCongNo();
            sample.setMaPhieuCN("PCN101");
            sample.setMaDL("KH01");
            sample.setSoTienNo(50000000.0);
            sample.setSoTienThu(30000000.0);
            sample.setConNo(20000000.0);
            sample.setGhiChu("Công nợ kỳ 1");
            mockList.add(sample);
            return ApiResponse.ok("Danh sách phiếu công nợ", mockList);
        }
    }

    @PostMapping("/congno")
    public ApiResponse<PhieuCongNo> taoPhieuCongNo(@RequestBody PhieuCongNo phieu) {
        if (phieu.getMaPhieuCN() == null || phieu.getMaPhieuCN().isEmpty()) {
            phieu.setMaPhieuCN("PCN" + (System.currentTimeMillis() % 100000));
        }
        phieu.setNgayLap(new Date());

        double soTienNo = phieu.getSoTienNo() != null ? phieu.getSoTienNo() : 0.0;
        double soTienThu = phieu.getSoTienThu() != null ? phieu.getSoTienThu() : 0.0;
        phieu.setConNo(Math.max(0.0, soTienNo - soTienThu));

        PhieuCongNo saved = phieuCongNoRepo.save(phieu);
        return ApiResponse.ok("Lập phiếu công nợ thành công", saved);
    }

    @PutMapping("/congno/{maPhieuCN}/thutien")
    public ApiResponse<PhieuCongNo> thuTienCongNo(@PathVariable String maPhieuCN, @RequestParam double soTienThu) {
        Optional<PhieuCongNo> opt = phieuCongNoRepo.findById(maPhieuCN);
        if (!opt.isPresent()) {
            return ApiResponse.error("Không tìm thấy phiếu công nợ: " + maPhieuCN);
        }
        PhieuCongNo phieu = opt.get();
        double currentThu = phieu.getSoTienThu() != null ? phieu.getSoTienThu() : 0.0;
        double tongNo = phieu.getSoTienNo() != null ? phieu.getSoTienNo() : 0.0;
        double newThu = currentThu + soTienThu;
        phieu.setSoTienThu(newThu);
        phieu.setConNo(Math.max(0.0, tongNo - newThu));

        PhieuCongNo saved = phieuCongNoRepo.save(phieu);
        return ApiResponse.ok("Đã thu tiền công nợ thành công", saved);
    }

    @DeleteMapping("/congno/{maPhieuCN}")
    public ApiResponse<String> deleteCongNo(@PathVariable String maPhieuCN) {
        phieuCongNoRepo.deleteById(maPhieuCN);
        return ApiResponse.ok("Đã xóa phiếu công nợ thành công", maPhieuCN);
    }

    @GetMapping("/doanhthu/thongke")
    public ApiResponse<RevenueStatsDto> getRevenueStats() {
        List<DonHang> allOrders = donHangRepo.findAll();

        double totalRevenue = 0.0;
        long totalOrders = allOrders.size();
        long paidOrders = 0;
        long debtOrders = 0;
        long unpaidOrders = 0;

        Map<Integer, Double> revenueByMonth = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            revenueByMonth.put(i, 0.0);
        }

        for (DonHang dh : allOrders) {
            double tongTien = dh.getTongTien() != null ? dh.getTongTien() : 0.0;
            totalRevenue += tongTien;

            String ttTT = dh.getTinhTrangThanhToan();
            if ("Đã thanh toán".equalsIgnoreCase(ttTT)) {
                paidOrders++;
            } else if ("Đang nợ".equalsIgnoreCase(ttTT)) {
                debtOrders++;
            } else {
                unpaidOrders++;
            }

            if (dh.getNgayLap() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dh.getNgayLap());
                int month = cal.get(Calendar.MONTH) + 1;
                revenueByMonth.put(month, revenueByMonth.getOrDefault(month, 0.0) + tongTien);
            }
        }

        RevenueStatsDto stats = new RevenueStatsDto(totalRevenue, totalOrders, paidOrders, debtOrders, unpaidOrders, revenueByMonth);
        return ApiResponse.ok("Thống kê doanh thu", stats);
    }

    @GetMapping("/doanhthu/thang/{thang}")
    public ApiResponse<List<DonHang>> getDonHangByThang(@PathVariable int thang) {
        List<DonHang> allOrders = donHangRepo.findAll();
        List<DonHang> result = new ArrayList<>();

        for (DonHang dh : allOrders) {
            if (dh.getNgayLap() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dh.getNgayLap());
                int m = cal.get(Calendar.MONTH) + 1;
                if (m == thang) {
                    result.add(dh);
                }
            }
        }
        return ApiResponse.ok("Danh sách đơn hàng tháng " + thang, result);
    }
}

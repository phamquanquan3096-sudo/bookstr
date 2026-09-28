package com.quanly.repository;

import com.quanly.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonHangRepository extends JpaRepository<DonHang, String> {
    List<DonHang> findByMaDL(String maDL);
    List<DonHang> findByMaGH(String maGH);
    List<DonHang> findByTrangThai(String trangThai);
    List<DonHang> findByTinhTrangGH(String tinhTrangGH);
}

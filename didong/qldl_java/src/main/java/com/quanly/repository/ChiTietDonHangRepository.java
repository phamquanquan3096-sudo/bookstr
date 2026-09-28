package com.quanly.repository;

import com.quanly.entity.ChiTietDonHang;
import com.quanly.entity.ChiTietDonHangId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, ChiTietDonHangId> {
    List<ChiTietDonHang> findByMaDH(String maDH);
    List<ChiTietDonHang> findByMaSP(String maSP);
    void deleteByMaDH(String maDH);
    void deleteByMaSP(String maSP);
}

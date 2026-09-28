package com.quanly.repository;

import com.quanly.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, String> {
    List<SanPham> findByTenSPContainingIgnoreCaseOrMaSPContainingIgnoreCase(String tenSP, String maSP);
}

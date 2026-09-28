package com.quanly.repository;

import com.quanly.entity.ChiTietKho;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChiTietKhoRepository extends JpaRepository<ChiTietKho, Long> {
    List<ChiTietKho> findByMaKho(String maKho);
    List<ChiTietKho> findByMaSP(String maSP);
    Optional<ChiTietKho> findByMaKhoAndMaSP(String maKho, String maSP);
    void deleteByMaSP(String maSP);
}

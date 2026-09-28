package com.quanly.repository;

import com.quanly.entity.PhieuCongNo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhieuCongNoRepository extends JpaRepository<PhieuCongNo, String> {
    List<PhieuCongNo> findByMaDL(String maDL);
}

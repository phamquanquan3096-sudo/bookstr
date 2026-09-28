package com.quanly.repository;

import com.quanly.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, String> {
    Optional<NhanVien> findByUserNameAndPassword(String userName, String password);
    Optional<NhanVien> findByUserName(String userName);
}

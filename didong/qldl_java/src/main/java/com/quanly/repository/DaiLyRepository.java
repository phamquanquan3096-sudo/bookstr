package com.quanly.repository;

import com.quanly.entity.DaiLy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DaiLyRepository extends JpaRepository<DaiLy, String> {
    Optional<DaiLy> findByUserNameAndPassword(String userName, String password);
    Optional<DaiLy> findByUserName(String userName);
    Optional<DaiLy> findByEmail(String email);
    Optional<DaiLy> findBySdt(String sdt);
    Optional<DaiLy> findByTenDL(String tenDL);
}

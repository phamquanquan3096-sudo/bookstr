package com.quanly.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ChucVu")
public class ChucVu {

    @Id
    @Column(name = "MaChucVu", length = 10)
    private String maChucVu;

    @Column(name = "ChucVu", length = 50)
    private String tenChucVu;

    public ChucVu() {}

    public ChucVu(String maChucVu, String tenChucVu) {
        this.maChucVu = maChucVu;
        this.tenChucVu = tenChucVu;
    }

    public String getMaChucVu() {
        return maChucVu;
    }

    public void setMaChucVu(String maChucVu) {
        this.maChucVu = maChucVu;
    }

    public String getTenChucVu() {
        return tenChucVu;
    }

    public void setTenChucVu(String tenChucVu) {
        this.tenChucVu = tenChucVu;
    }
}

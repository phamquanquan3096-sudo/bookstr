package com.quanly.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "LoaiDL")
public class LoaiDL {

    @Id
    @Column(name = "MaLoaiDL", length = 10)
    private String maLoaiDL;

    @Column(name = "TenDaiLy", length = 50)
    private String tenLoaiDL;

    @Column(name = "ChietKhau")
    private Double chietKhau;

    public LoaiDL() {}

    public LoaiDL(String maLoaiDL, String tenLoaiDL, Double chietKhau) {
        this.maLoaiDL = maLoaiDL;
        this.tenLoaiDL = tenLoaiDL;
        this.chietKhau = chietKhau;
    }

    public String getMaLoaiDL() {
        return maLoaiDL;
    }

    public void setMaLoaiDL(String maLoaiDL) {
        this.maLoaiDL = maLoaiDL;
    }

    public String getTenLoaiDL() {
        return tenLoaiDL;
    }

    public void setTenLoaiDL(String tenLoaiDL) {
        this.tenLoaiDL = tenLoaiDL;
    }

    public Double getChietKhau() {
        return chietKhau;
    }

    public void setChietKhau(Double chietKhau) {
        this.chietKhau = chietKhau;
    }
}

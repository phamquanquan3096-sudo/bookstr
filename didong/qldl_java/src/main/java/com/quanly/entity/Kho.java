package com.quanly.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Kho")
public class Kho {

    @Id
    @Column(name = "MaKho", length = 10)
    private String maKho;

    @Column(name = "TenKho", length = 100)
    private String tenKho;

    @Column(name = "DiaChi", length = 200)
    private String diaChi;

    public Kho() {}

    public Kho(String maKho, String tenKho, String diaChi) {
        this.maKho = maKho;
        this.tenKho = tenKho;
        this.diaChi = diaChi;
    }

    public String getMaKho() {
        return maKho;
    }

    public void setMaKho(String maKho) {
        this.maKho = maKho;
    }

    public String getTenKho() {
        return tenKho;
    }

    public void setTenKho(String tenKho) {
        this.tenKho = tenKho;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}

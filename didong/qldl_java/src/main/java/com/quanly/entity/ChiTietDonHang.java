package com.quanly.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ChiTietDonHang")
@IdClass(ChiTietDonHangId.class)
public class ChiTietDonHang {

    @Id
    @Column(name = "MaDH", length = 10)
    private String maDH;

    @Id
    @Column(name = "MaSP", length = 10)
    private String maSP;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Transient
    private Double donGia;

    @Transient
    private String tenSP;

    @Column(name = "ThanhTien")
    private Double thanhTien;

    public ChiTietDonHang() {}

    public ChiTietDonHang(String maDH, String maSP, Integer soLuong, Double donGia, Double thanhTien) {
        this.maDH = maDH;
        this.maSP = maSP;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public String getMaDH() {
        return maDH;
    }

    public void setMaDH(String maDH) {
        this.maDH = maDH;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Double getDonGia() {
        return donGia;
    }

    public void setDonGia(Double donGia) {
        this.donGia = donGia;
    }

    public String getTenSP() {
        return tenSP;
    }

    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }

    public Double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(Double thanhTien) {
        this.thanhTien = thanhTien;
    }
}

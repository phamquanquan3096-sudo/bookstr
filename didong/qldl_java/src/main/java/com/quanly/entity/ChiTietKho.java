package com.quanly.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ChiTietKho")
public class ChiTietKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STT")
    private Long id;

    @Column(name = "MaKho", length = 10)
    private String maKho;

    @Column(name = "MaSP", length = 10)
    private String maSP;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "TinhTrang", length = 50)
    private String tinhTrang;

    public ChiTietKho() {}

    public ChiTietKho(String maKho, String maSP, Integer soLuong) {
        this.maKho = maKho;
        this.maSP = maSP;
        this.soLuong = soLuong;
        this.tinhTrang = (soLuong == null || soLuong <= 0) ? "Hết hàng" : (soLuong < 10 ? "Sắp hết hàng" : "Tồn kho");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaKho() {
        return maKho;
    }

    public void setMaKho(String maKho) {
        this.maKho = maKho;
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

    public String getTinhTrang() {
        if (tinhTrang != null && !tinhTrang.isEmpty()) {
            return tinhTrang;
        }
        return (soLuong == null || soLuong <= 0) ? "Hết hàng" : (soLuong < 10 ? "Sắp hết hàng" : "Tồn kho");
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }
}

package com.quanly.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PhieuCongNo")
public class PhieuCongNo {

    @Id
    @Column(name = "MaPhieuCN", length = 10)
    private String maPhieuCN;

    @Column(name = "MaDL", length = 10)
    private String maDL;

    @Column(name = "NgayLap")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayLap;

    @Column(name = "SoTienNo")
    private Double soTienNo;

    @Column(name = "SoTienThu")
    private Double soTienThu;

    @Column(name = "ConNo")
    private Double conNo;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    public PhieuCongNo() {}

    public String getMaPhieuCN() {
        return maPhieuCN;
    }

    public void setMaPhieuCN(String maPhieuCN) {
        this.maPhieuCN = maPhieuCN;
    }

    public String getMaDL() {
        return maDL;
    }

    public void setMaDL(String maDL) {
        this.maDL = maDL;
    }

    public Date getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Date ngayLap) {
        this.ngayLap = ngayLap;
    }

    public Double getSoTienNo() {
        return soTienNo;
    }

    public void setSoTienNo(Double soTienNo) {
        this.soTienNo = soTienNo;
    }

    public Double getSoTienThu() {
        return soTienThu;
    }

    public void setSoTienThu(Double soTienThu) {
        this.soTienThu = soTienThu;
    }

    public Double getConNo() {
        return conNo;
    }

    public void setConNo(Double conNo) {
        this.conNo = conNo;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

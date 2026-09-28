package com.quanly.app.model;

import java.io.Serializable;

public class PhieuCongNo implements Serializable {
    private String maPhieuCN;
    private String maDL;
    private String ngayLap;
    private Double soTienNo;
    private Double soTienThu;
    private Double conNo;
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

    public String getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(String ngayLap) {
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

package com.quanly.app.model;

import java.io.Serializable;

public class DonHang implements Serializable {
    private String maDH;
    private String maDL;
    private String ngayLap;
    private String trangThai;
    private String tinhTrangThanhToan;
    private String diemGiao;
    private Double tongTien;
    private String tinhTrangGH;
    private String maGH;

    public DonHang() {}

    public String getMaDH() {
        return maDH;
    }

    public void setMaDH(String maDH) {
        this.maDH = maDH;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTinhTrangThanhToan() {
        return tinhTrangThanhToan;
    }

    public void setTinhTrangThanhToan(String tinhTrangThanhToan) {
        this.tinhTrangThanhToan = tinhTrangThanhToan;
    }

    public String getDiemGiao() {
        return diemGiao;
    }

    public void setDiemGiao(String diemGiao) {
        this.diemGiao = diemGiao;
    }

    public Double getTongTien() {
        return tongTien;
    }

    public void setTongTien(Double tongTien) {
        this.tongTien = tongTien;
    }

    public String getTinhTrangGH() {
        return tinhTrangGH;
    }

    public void setTinhTrangGH(String tinhTrangGH) {
        this.tinhTrangGH = tinhTrangGH;
    }

    public String getMaGH() {
        return maGH;
    }

    public void setMaGH(String maGH) {
        this.maGH = maGH;
    }
}

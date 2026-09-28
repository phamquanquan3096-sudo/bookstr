package com.quanly.app.model;

import java.io.Serializable;

public class SanPham implements Serializable {
    private String maSP;
    private String tenSP;
    private String donViTinh;
    private Integer gia;
    private Integer tongTon;
    private String hinhAnh;
    private String ngaySX;
    private String hanSD;

    public SanPham() {}

    public SanPham(String maSP, String tenSP, String donViTinh, Integer gia, Integer tongTon) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.donViTinh = donViTinh;
        this.gia = gia;
        this.tongTon = tongTon;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public String getTenSP() {
        return tenSP;
    }

    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public Integer getGia() {
        return gia;
    }

    public void setGia(Integer gia) {
        this.gia = gia;
    }

    public Integer getTongTon() {
        return tongTon;
    }

    public void setTongTon(Integer tongTon) {
        this.tongTon = tongTon;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getNgaySX() {
        return ngaySX;
    }

    public void setNgaySX(String ngaySX) {
        this.ngaySX = ngaySX;
    }

    public String getHanSD() {
        return hanSD;
    }

    public void setHanSD(String hanSD) {
        this.hanSD = hanSD;
    }
}

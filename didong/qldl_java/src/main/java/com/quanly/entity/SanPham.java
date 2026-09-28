package com.quanly.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SanPham")
public class SanPham {

    @Id
    @Column(name = "MaSP", length = 10)
    private String maSP;

    @Column(name = "TenSP", length = 100)
    private String tenSP;

    @Column(name = "DonViTinh", length = 20)
    private String donViTinh;

    @Column(name = "Gia")
    private Integer gia;

    @Column(name = "HanSD")
    @Temporal(TemporalType.DATE)
    private Date hanSD;

    @Column(name = "NgaySX")
    @Temporal(TemporalType.DATE)
    private Date ngaySX;

    @Column(name = "TongTon")
    private Integer tongTon;

    @Column(name = "HinhAnh", length = 255)
    private String hinhAnh;

    public SanPham() {}

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

    public Date getHanSD() {
        return hanSD;
    }

    public void setHanSD(Date hanSD) {
        this.hanSD = hanSD;
    }

    public Date getNgaySX() {
        return ngaySX;
    }

    public void setNgaySX(Date ngaySX) {
        this.ngaySX = ngaySX;
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
}

package com.quanly.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "DaiLy")
public class DaiLy {

    @Id
    @Column(name = "MaDL", length = 10)
    private String maDL;

    @Column(name = "MaLoaiDL", length = 10)
    private String maLoaiDL;

    @Column(name = "UserName", length = 50)
    private String userName;

    @Column(name = "Password", length = 100)
    private String password;

    @Column(name = "TenDL", length = 100)
    private String tenDL;

    @Column(name = "SDT", length = 20)
    private String sdt;

    @Column(name = "DiaChi", length = 200)
    private String diaChi;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "NgayTao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "HinhAnh", length = 255)
    private String hinhAnh;

    @Column(name = "TongDoanhSo")
    private Double tongDoanhSo;

    public DaiLy() {}

    public String getMaDL() {
        return maDL;
    }

    public void setMaDL(String maDL) {
        this.maDL = maDL;
    }

    public String getMaLoaiDL() {
        return maLoaiDL;
    }

    public void setMaLoaiDL(String maLoaiDL) {
        this.maLoaiDL = maLoaiDL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTenDL() {
        return tenDL;
    }

    public void setTenDL(String tenDL) {
        this.tenDL = tenDL;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public Double getTongDoanhSo() {
        return tongDoanhSo;
    }

    public void setTongDoanhSo(Double tongDoanhSo) {
        this.tongDoanhSo = tongDoanhSo;
    }
}

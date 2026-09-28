package com.quanly.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "DonHang")
public class DonHang {

    @Id
    @Column(name = "MaDH", length = 10)
    private String maDH;

    @Column(name = "MaDL", length = 10)
    private String maDL;

    @Column(name = "MaNVLap", length = 10)
    private String maNVLap;

    @Column(name = "NgayLap")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayLap;

    @Column(name = "TrangThai", length = 50)
    private String trangThai; // "Mới tạo", "Đã duyệt", "Đã xuất kho", "Đã hủy"

    @Column(name = "TinhTrangThanhToan", length = 50)
    private String tinhTrangThanhToan; // "Chưa thanh toán", "Đã thanh toán"

    @Column(name = "DiemGiao", length = 200)
    private String diemGiao;

    @Column(name = "TongTien")
    private Double tongTien;

    @Column(name = "PhieuXuatKho")
    private Boolean phieuXuatKho;

    @Column(name = "XuatHoaDon")
    private Boolean xuatHoaDon;

    @Column(name = "TinhTrangGH", length = 50)
    private String tinhTrangGH; // "Chờ phân công", "Đang giao", "Đã giao"

    @Column(name = "MaGH", length = 10)
    private String maGH; // Mã NVGH được giao

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

    public String getMaNVLap() {
        return maNVLap;
    }

    public void setMaNVLap(String maNVLap) {
        this.maNVLap = maNVLap;
    }

    public Date getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Date ngayLap) {
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

    public Boolean getPhieuXuatKho() {
        return phieuXuatKho;
    }

    public void setPhieuXuatKho(Boolean phieuXuatKho) {
        this.phieuXuatKho = phieuXuatKho;
    }

    public Boolean getXuatHoaDon() {
        return xuatHoaDon;
    }

    public void setXuatHoaDon(Boolean xuatHoaDon) {
        this.xuatHoaDon = xuatHoaDon;
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

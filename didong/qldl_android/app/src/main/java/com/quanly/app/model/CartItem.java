package com.quanly.app.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private SanPham sanPham;
    private int soLuong;

    public CartItem() {}

    public CartItem(SanPham sanPham, int soLuong) {
        this.sanPham = sanPham;
        this.soLuong = soLuong;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getThanhTien() {
        if (sanPham != null && sanPham.getGia() != null) {
            return sanPham.getGia() * soLuong;
        }
        return 0;
    }
}

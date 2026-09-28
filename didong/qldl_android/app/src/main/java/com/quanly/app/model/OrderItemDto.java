package com.quanly.app.model;

public class OrderItemDto {
    private String maSP;
    private Integer soLuong;

    public OrderItemDto(String maSP, Integer soLuong) {
        this.maSP = maSP;
        this.soLuong = soLuong;
    }

    public String getMaSP() {
        return maSP;
    }

    public Integer getSoLuong() {
        return soLuong;
    }
}

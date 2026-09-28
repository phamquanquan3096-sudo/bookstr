package com.quanly.dto;

import java.util.List;

public class OrderCreateRequest {
    private String maDL;
    private String diemGiao;
    private List<OrderItemDto> items;

    public OrderCreateRequest() {}

    public String getMaDL() {
        return maDL;
    }

    public void setMaDL(String maDL) {
        this.maDL = maDL;
    }

    public String getDiemGiao() {
        return diemGiao;
    }

    public void setDiemGiao(String diemGiao) {
        this.diemGiao = diemGiao;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
}

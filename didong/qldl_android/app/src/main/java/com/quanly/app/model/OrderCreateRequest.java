package com.quanly.app.model;

import java.util.List;

public class OrderCreateRequest {
    private String maDL;
    private String diemGiao;
    private List<OrderItemDto> items;

    public OrderCreateRequest(String maDL, String diemGiao, List<OrderItemDto> items) {
        this.maDL = maDL;
        this.diemGiao = diemGiao;
        this.items = items;
    }

    public String getMaDL() {
        return maDL;
    }

    public String getDiemGiao() {
        return diemGiao;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }
}

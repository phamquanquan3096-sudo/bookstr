package com.quanly.app.model;

import java.util.Map;

public class RevenueStats {
    private Double totalRevenue;
    private Long totalOrders;
    private Long paidOrders;
    private Long debtOrders;
    private Long unpaidOrders;
    private Map<Integer, Double> revenueByMonth;

    public RevenueStats() {}

    public Double getTotalRevenue() {
        return totalRevenue != null ? totalRevenue : 0.0;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders != null ? totalOrders : 0L;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getPaidOrders() {
        return paidOrders != null ? paidOrders : 0L;
    }

    public void setPaidOrders(Long paidOrders) {
        this.paidOrders = paidOrders;
    }

    public Long getDebtOrders() {
        return debtOrders != null ? debtOrders : 0L;
    }

    public void setDebtOrders(Long debtOrders) {
        this.debtOrders = debtOrders;
    }

    public Long getUnpaidOrders() {
        return unpaidOrders != null ? unpaidOrders : 0L;
    }

    public void setUnpaidOrders(Long unpaidOrders) {
        this.unpaidOrders = unpaidOrders;
    }

    public Map<Integer, Double> getRevenueByMonth() {
        return revenueByMonth;
    }

    public void setRevenueByMonth(Map<Integer, Double> revenueByMonth) {
        this.revenueByMonth = revenueByMonth;
    }
}

package com.quanly.dto;

import java.util.Map;

public class RevenueStatsDto {

    private Double totalRevenue;
    private Long totalOrders;
    private Long paidOrders;
    private Long debtOrders;
    private Long unpaidOrders;
    private Map<Integer, Double> revenueByMonth;

    public RevenueStatsDto() {}

    public RevenueStatsDto(Double totalRevenue, Long totalOrders, Long paidOrders, Long debtOrders, Long unpaidOrders, Map<Integer, Double> revenueByMonth) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.paidOrders = paidOrders;
        this.debtOrders = debtOrders;
        this.unpaidOrders = unpaidOrders;
        this.revenueByMonth = revenueByMonth;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getPaidOrders() {
        return paidOrders;
    }

    public void setPaidOrders(Long paidOrders) {
        this.paidOrders = paidOrders;
    }

    public Long getDebtOrders() {
        return debtOrders;
    }

    public void setDebtOrders(Long debtOrders) {
        this.debtOrders = debtOrders;
    }

    public Long getUnpaidOrders() {
        return unpaidOrders;
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

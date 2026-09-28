package com.quanly.entity;

import java.io.Serializable;
import java.util.Objects;

public class ChiTietDonHangId implements Serializable {
    private String maDH;
    private String maSP;

    public ChiTietDonHangId() {}

    public ChiTietDonHangId(String maDH, String maSP) {
        this.maDH = maDH;
        this.maSP = maSP;
    }

    public String getMaDH() {
        return maDH;
    }

    public void setMaDH(String maDH) {
        this.maDH = maDH;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietDonHangId that = (ChiTietDonHangId) o;
        return Objects.equals(maDH, that.maDH) && Objects.equals(maSP, that.maSP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDH, maSP);
    }
}

package com.quanly.app.model;

import java.io.Serializable;

public class TheLoaiSach implements Serializable {
    private String maLoai;
    private String tenLoai;
    private String moTa;
    private int soLuongSach;
    private String iconName; // e.g., "ic_menu_agenda", "ic_menu_edit"
    private String colorHex; // e.g., "#3F51B5", "#FF5722"
    private String keywords; // search keywords for products in this category

    public TheLoaiSach() {}

    public TheLoaiSach(String maLoai, String tenLoai, String moTa, int soLuongSach, String colorHex, String keywords) {
        this.maLoai = maLoai;
        this.tenLoai = tenLoai;
        this.moTa = moTa;
        this.soLuongSach = soLuongSach;
        this.colorHex = colorHex;
        this.keywords = keywords;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getSoLuongSach() {
        return soLuongSach;
    }

    public void setSoLuongSach(int soLuongSach) {
        this.soLuongSach = soLuongSach;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getColorHex() {
        return colorHex != null && !colorHex.isEmpty() ? colorHex : "#1E88E5";
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getKeywords() {
        return keywords != null ? keywords : "";
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}

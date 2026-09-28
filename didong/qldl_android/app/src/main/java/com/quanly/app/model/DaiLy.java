package com.quanly.app.model;

import java.io.Serializable;

public class DaiLy implements Serializable {
    private String maDL;
    private String maLoaiDL;
    private String userName;
    private String password;
    private String tenDL;
    private String sdt;
    private String diaChi;
    private String email;

    public DaiLy() {}

    public DaiLy(String userName, String password, String tenDL, String sdt, String diaChi, String email) {
        this.userName = userName;
        this.password = password;
        this.tenDL = tenDL;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.email = email;
    }

    public String getMaDL() { return maDL; }
    public void setMaDL(String maDL) { this.maDL = maDL; }

    public String getMaLoaiDL() { return maLoaiDL; }
    public void setMaLoaiDL(String maLoaiDL) { this.maLoaiDL = maLoaiDL; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTenDL() { return tenDL; }
    public void setTenDL(String tenDL) { this.tenDL = tenDL; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

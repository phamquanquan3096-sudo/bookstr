package com.quanly.app.model;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BankInfo implements Serializable {
    private String bankCode;
    private String bankName;
    private String shortName;
    private String accountNumber;
    private String accountName;
    private String appPackage;
    private int iconResId;

    public BankInfo(String bankCode, String shortName, String bankName, String accountNumber, String accountName, String appPackage) {
        this.bankCode = bankCode;
        this.shortName = shortName;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.appPackage = appPackage;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getShortName() {
        return shortName;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public String getVietQrUrl(String orderId, double amount) {
        try {
            long amt = Math.round(amount);
            String desc = (orderId != null ? orderId : "DH") + " Thanh toan sach";
            String encodedDesc = URLEncoder.encode(desc, StandardCharsets.UTF_8.toString());
            String encodedName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString());
            return "https://img.vietqr.io/image/" + bankCode + "-" + accountNumber + "-compact2.jpg?amount=" 
                    + amt + "&addInfo=" + encodedDesc + "&accountName=" + encodedName;
        } catch (Exception e) {
            return "https://img.vietqr.io/image/" + bankCode + "-" + accountNumber + "-compact2.jpg?amount=" 
                    + Math.round(amount) + "&addInfo=" + orderId;
        }
    }

    public static List<BankInfo> getDefaultBanks() {
        List<BankInfo> list = new ArrayList<>();
        list.add(new BankInfo("MB", "MB Bank", "MB Bank - Ngân hàng Quân Đội", "999988886666", "CTY PHAT HANH SACH VN", "com.mbmobile"));
        list.add(new BankInfo("VCB", "Vietcombank", "Vietcombank - TMCP Ngoại Thương VN", "1018899888", "CTY PHAT HANH SACH VN", "com.VCB"));
        list.add(new BankInfo("TCB", "Techcombank", "Techcombank - TMCP Kỹ Thương VN", "1903666888999", "CTY PHAT HANH SACH VN", "vn.com.techcombank.bb.app"));
        list.add(new BankInfo("ICB", "VietinBank", "VietinBank - TMCP Công Thương VN", "113000889966", "CTY PHAT HANH SACH VN", "com.vietinbank.ipay"));
        list.add(new BankInfo("BIDV", "BIDV", "BIDV - Đầu tư và Phát triển VN", "12410008889999", "CTY PHAT HANH SACH VN", "com.vnpay.bidv"));
        list.add(new BankInfo("MOMO", "Ví MoMo", "Ví Điện Tử MoMo (Quét QR Pay)", "0909123456", "CTY PHAT HANH SACH VN", "com.mservice.momopay"));
        list.add(new BankInfo("ZALOPAY", "ZaloPay", "Ví Điện Tử ZaloPay (QR Code)", "0909123456", "CTY PHAT HANH SACH VN", "vn.com.vng.zalopay"));
        return list;
    }

    @Override
    public String toString() {
        return shortName + " (" + accountNumber + ")";
    }
}

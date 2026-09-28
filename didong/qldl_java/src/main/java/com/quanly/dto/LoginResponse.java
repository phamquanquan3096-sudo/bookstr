package com.quanly.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String userType; // "NVKD", "NVKT", "NVK", "QTHT", "NVGH", "DAILY"
    private String userId;   // MaNV hoặc MaDL
    private String fullName; // TenNV hoặc TenDL
    private String token;

    public LoginResponse() {}

    public LoginResponse(boolean success, String message, String userType, String userId, String fullName) {
        this.success = success;
        this.message = message;
        this.userType = userType;
        this.userId = userId;
        this.fullName = fullName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

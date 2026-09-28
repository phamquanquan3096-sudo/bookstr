package com.quanly.app.model;

public class LoginResponse {
    private boolean success;
    private String message;
    private String userType;
    private String userId;
    private String fullName;
    private String token;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUserType() {
        return userType;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getToken() {
        return token;
    }
}

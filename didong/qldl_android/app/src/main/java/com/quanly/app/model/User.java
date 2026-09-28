package com.quanly.app.model;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String fullName;
    private String userType;
    private String token;

    public User() {}

    public User(String userId, String fullName, String userType, String token) {
        this.userId = userId;
        this.fullName = fullName;
        this.userType = userType;
        this.token = token;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

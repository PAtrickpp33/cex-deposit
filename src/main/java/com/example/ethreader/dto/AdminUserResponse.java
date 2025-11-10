package com.example.ethreader.dto;

import com.example.ethreader.model.User;
import java.time.LocalDateTime;

public class AdminUserResponse {
    private String id;
    private String username;
    private String email;
    private User.UserRole role;
    private LocalDateTime createdAt;
    private long walletCount;

    public AdminUserResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User.UserRole getRole() {
        return role;
    }

    public void setRole(User.UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getWalletCount() {
        return walletCount;
    }

    public void setWalletCount(long walletCount) {
        this.walletCount = walletCount;
    }
}


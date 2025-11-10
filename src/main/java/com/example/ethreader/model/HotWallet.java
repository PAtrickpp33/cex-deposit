package com.example.ethreader.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "hot_wallets")
public class HotWallet {
    @Id
    private String id;
    private String userId;
    private String address;
    private String privateKey; // Encrypted private key
    private String chain; // e.g., "sepolia"
    private String tokenAddress; // null for native ETH, contract address for ERC20
    private LocalDateTime createdAt;
    private boolean active; // Whether this wallet is currently active

    public HotWallet() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public HotWallet(String userId, String address, String privateKey, String chain, String tokenAddress) {
        this.userId = userId;
        this.address = address;
        this.privateKey = privateKey;
        this.chain = chain;
        this.tokenAddress = tokenAddress;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}


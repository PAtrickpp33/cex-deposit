package com.example.ethreader.dto;

import com.example.ethreader.model.DepositTransaction;
import java.math.BigInteger;
import java.time.LocalDateTime;

public class DepositResponse {
    private String id;
    private String transactionHash;
    private String walletAddress;
    private String userId;
    private BigInteger amount;
    private String tokenAddress;
    private BigInteger blockNumber;
    private int confirmations;
    private DepositTransaction.DepositStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String etherscanUrl; // Etherscan link for viewing transaction

    public DepositResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public DepositTransaction.DepositStatus getStatus() {
        return status;
    }

    public void setStatus(DepositTransaction.DepositStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getEtherscanUrl() {
        return etherscanUrl;
    }

    public void setEtherscanUrl(String etherscanUrl) {
        this.etherscanUrl = etherscanUrl;
    }
}


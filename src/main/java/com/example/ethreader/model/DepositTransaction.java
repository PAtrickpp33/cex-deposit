package com.example.ethreader.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Document(collection = "deposit_transactions")
public class DepositTransaction {
    @Id
    private String id;
    private String transactionHash;
    private String walletAddress;
    private String userId;
    private BigInteger amount; // Amount in wei
    private String tokenAddress; // null for native ETH
    private String chain; // e.g., "sepolia", "mainnet"
    private BigInteger blockNumber;
    private int confirmations;
    private DepositStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public enum DepositStatus {
        PENDING,      // Detected but not confirmed
        CONFIRMING,   // Confirming (1-11 blocks)
        CONFIRMED,    // 12+ confirmations, ready to credit
        CREDITED,     // Successfully credited to user
        FAILED        // Processing failed
    }

    public DepositTransaction() {
        this.createdAt = LocalDateTime.now();
        this.status = DepositStatus.PENDING;
        this.confirmations = 0;
    }

    public DepositTransaction(String transactionHash, String walletAddress, String userId, 
                             BigInteger amount, String tokenAddress, BigInteger blockNumber) {
        this.transactionHash = transactionHash;
        this.walletAddress = walletAddress;
        this.userId = userId;
        this.amount = amount;
        this.tokenAddress = tokenAddress;
        this.blockNumber = blockNumber;
        this.createdAt = LocalDateTime.now();
        this.status = DepositStatus.PENDING;
        this.confirmations = 0;
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

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
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

    public DepositStatus getStatus() {
        return status;
    }

    public void setStatus(DepositStatus status) {
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
}


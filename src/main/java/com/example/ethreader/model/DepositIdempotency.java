package com.example.ethreader.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Document(collection = "deposit_idempotency")
public class DepositIdempotency {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String idempotencyKey; // transactionHash + "_" + blockNumber
    
    private String transactionHash;
    private BigInteger blockNumber;
    private LocalDateTime processedAt;

    public DepositIdempotency() {
        this.processedAt = LocalDateTime.now();
    }

    public DepositIdempotency(String transactionHash, BigInteger blockNumber) {
        this.transactionHash = transactionHash;
        this.blockNumber = blockNumber;
        this.idempotencyKey = transactionHash + "_" + blockNumber.toString();
        this.processedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}


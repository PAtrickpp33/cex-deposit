package com.example.ethreader.dto;

public class TransactionResponse {
    private String transactionHash;
    private String status; // SUCCESS, FAILED, PENDING
    private String message;
    private String etherscanUrl;

    public TransactionResponse() {
    }

    public TransactionResponse(String transactionHash, String status, String message) {
        this.transactionHash = transactionHash;
        this.status = status;
        this.message = message;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEtherscanUrl() {
        return etherscanUrl;
    }

    public void setEtherscanUrl(String etherscanUrl) {
        this.etherscanUrl = etherscanUrl;
    }
}


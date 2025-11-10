package com.example.ethreader.dto;

import java.math.BigInteger;

public class SendTransactionRequest {
    private String toAddress;
    private BigInteger amount; // Amount in wei
    private String tokenAddress; // null for native ETH, contract address for ERC20
    private BigInteger gasPrice; // Optional, will be estimated if not provided
    private BigInteger gasLimit; // Optional, will be estimated if not provided

    public SendTransactionRequest() {
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
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

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }
}


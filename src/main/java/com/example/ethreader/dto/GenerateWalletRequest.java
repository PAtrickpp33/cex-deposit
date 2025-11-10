package com.example.ethreader.dto;

public class GenerateWalletRequest {
    private String chain;
    private String tokenAddress; // null for native ETH

    public GenerateWalletRequest() {
    }

    public GenerateWalletRequest(String chain, String tokenAddress) {
        this.chain = chain;
        this.tokenAddress = tokenAddress;
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
}


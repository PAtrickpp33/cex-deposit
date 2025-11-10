package com.example.ethreader.dto;

public class AddressResponse {
    private String address;
    private String chain;
    private String tokenAddress;
    private String qrCodeData; // Base64 encoded QR code image

    public AddressResponse() {
    }

    public AddressResponse(String address, String chain, String tokenAddress) {
        this.address = address;
        this.chain = chain;
        this.tokenAddress = tokenAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }
}


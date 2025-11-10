package com.example.ethreader.controller;

import com.example.ethreader.dto.AddressResponse;
import com.example.ethreader.dto.DepositResponse;
import com.example.ethreader.dto.GenerateWalletRequest;
import com.example.ethreader.model.DepositTransaction;
import com.example.ethreader.model.HotWallet;
import com.example.ethreader.repository.DepositTransactionRepository;
import com.example.ethreader.service.UserService;
import com.example.ethreader.service.WalletService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deposit")
@CrossOrigin(origins = "*")
public class DepositController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    private String getCurrentUserId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @GetMapping("/address")
    public ResponseEntity<?> getDepositAddress(
            @RequestParam String chain,
            @RequestParam(required = false) String tokenAddress,
            Authentication authentication) {
        try {
            String userId = getCurrentUserId(authentication);
            
            // Get or generate wallet
            HotWallet wallet = walletService.getActiveWallet(userId, chain, tokenAddress)
                    .orElseGet(() -> walletService.generateHotWallet(userId, chain, tokenAddress));

            AddressResponse response = new AddressResponse();
            response.setAddress(wallet.getAddress());
            response.setChain(wallet.getChain());
            response.setTokenAddress(wallet.getTokenAddress());
            response.setQrCodeData(generateQRCode(wallet.getAddress()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/current-address")
    public ResponseEntity<?> getCurrentAddress(
            @RequestParam String chain,
            @RequestParam(required = false) String tokenAddress,
            Authentication authentication) {
        try {
            String userId = getCurrentUserId(authentication);
            
            // Get current active wallet (don't generate new one)
            HotWallet wallet = walletService.getActiveWallet(userId, chain, tokenAddress)
                    .orElse(null);

            if (wallet == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No active wallet found. Please generate a new address.");
                return ResponseEntity.badRequest().body(error);
            }

            AddressResponse response = new AddressResponse();
            response.setAddress(wallet.getAddress());
            response.setChain(wallet.getChain());
            response.setTokenAddress(wallet.getTokenAddress());
            response.setQrCodeData(generateQRCode(wallet.getAddress()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/refresh-address")
    public ResponseEntity<?> refreshDepositAddress(
            @RequestBody GenerateWalletRequest request,
            Authentication authentication) {
        try {
            String userId = getCurrentUserId(authentication);
            
            // Generate new wallet (refresh)
            HotWallet wallet = walletService.refreshWallet(
                    userId,
                    request.getChain(),
                    request.getTokenAddress()
            );

            AddressResponse response = new AddressResponse();
            response.setAddress(wallet.getAddress());
            response.setChain(wallet.getChain());
            response.setTokenAddress(wallet.getTokenAddress());
            response.setQrCodeData(generateQRCode(wallet.getAddress()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingDeposits(Authentication authentication) {
        try {
            String userId = getCurrentUserId(authentication);
            
            List<DepositTransaction.DepositStatus> pendingStatuses = List.of(
                    DepositTransaction.DepositStatus.PENDING,
                    DepositTransaction.DepositStatus.CONFIRMING,
                    DepositTransaction.DepositStatus.CONFIRMED
            );
            
            List<DepositTransaction> deposits = depositTransactionRepository
                    .findByUserIdAndStatusInOrderByCreatedAtDesc(userId, pendingStatuses);
            
            List<DepositResponse> responses = deposits.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getDepositHistory(Authentication authentication) {
        try {
            String userId = getCurrentUserId(authentication);
            
            List<DepositTransaction> deposits = depositTransactionRepository
                    .findByUserIdOrderByCreatedAtDesc(userId);
            
            List<DepositResponse> responses = deposits.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private DepositResponse convertToResponse(DepositTransaction deposit) {
        DepositResponse response = new DepositResponse();
        response.setId(deposit.getId());
        response.setTransactionHash(deposit.getTransactionHash());
        response.setWalletAddress(deposit.getWalletAddress());
        response.setAmount(deposit.getAmount());
        response.setTokenAddress(deposit.getTokenAddress());
        response.setBlockNumber(deposit.getBlockNumber());
        response.setConfirmations(deposit.getConfirmations());
        response.setStatus(deposit.getStatus());
        response.setCreatedAt(deposit.getCreatedAt());
        response.setProcessedAt(deposit.getProcessedAt());
        
        // Generate Etherscan URL based on chain
        String etherscanUrl = generateEtherscanUrl(deposit.getTransactionHash(), deposit.getChain());
        response.setEtherscanUrl(etherscanUrl);
        
        return response;
    }

    private String generateEtherscanUrl(String transactionHash, String chain) {
        // Determine Etherscan URL based on chain
        String baseUrl;
        if (chain != null && chain.toLowerCase().contains("sepolia")) {
            baseUrl = "https://sepolia.etherscan.io/tx/";
        } else if (chain != null && chain.toLowerCase().contains("mainnet")) {
            baseUrl = "https://etherscan.io/tx/";
        } else {
            // Default to Sepolia for now
            baseUrl = "https://sepolia.etherscan.io/tx/";
        }
        return baseUrl + transactionHash;
    }

    private String generateQRCode(String address) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(address, BarcodeFormat.QR_CODE, 200, 200);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }
}


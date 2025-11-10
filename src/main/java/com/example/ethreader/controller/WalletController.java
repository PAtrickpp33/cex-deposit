package com.example.ethreader.controller;

import com.example.ethreader.dto.AddressResponse;
import com.example.ethreader.dto.GenerateWalletRequest;
import com.example.ethreader.dto.WalletResponse;
import com.example.ethreader.model.HotWallet;
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
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private com.example.ethreader.service.UserService userService;

    private String getCurrentUserId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateWallet(
            @RequestBody GenerateWalletRequest request,
            Authentication authentication) {
        try {
            String userId = getCurrentUserId(authentication);
            HotWallet wallet = walletService.generateHotWallet(
                    userId,
                    request.getChain(),
                    request.getTokenAddress()
            );

            WalletResponse response = convertToResponse(wallet);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshWallet(
            @RequestBody GenerateWalletRequest request,
            Authentication authentication) {
        try {
            String userId = getCurrentUserId(authentication);
            HotWallet wallet = walletService.refreshWallet(
                    userId,
                    request.getChain(),
                    request.getTokenAddress()
            );

            WalletResponse response = convertToResponse(wallet);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listWallets(Authentication authentication) {
        try {
            String userId = getCurrentUserId(authentication);
            List<HotWallet> wallets = walletService.getUserWallets(userId);
            
            List<WalletResponse> responses = wallets.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/private-key")
    public ResponseEntity<?> getPrivateKey(
            @RequestParam String walletId,
            Authentication authentication) {
        try {
            String userId = getCurrentUserId(authentication);
            
            // Get wallet by ID and verify it belongs to the user
            HotWallet wallet = walletService.getUserWallets(userId).stream()
                    .filter(w -> w.getId().equals(walletId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));
            
            // Decrypt private key
            String decryptedPrivateKey = walletService.decryptPrivateKey(wallet.getPrivateKey());
            
            // Ensure format is correct for MetaMask (64 hex characters, no 0x prefix)
            String privateKey = decryptedPrivateKey;
            if (privateKey.startsWith("0x")) {
                privateKey = privateKey.substring(2);
            }
            // Pad to 64 characters if needed
            if (privateKey.length() < 64) {
                privateKey = String.format("%064s", privateKey).replace(' ', '0');
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("privateKey", privateKey);
            response.put("address", wallet.getAddress());
            response.put("chain", wallet.getChain());
            response.put("tokenAddress", wallet.getTokenAddress() != null ? wallet.getTokenAddress() : "");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private WalletResponse convertToResponse(HotWallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setAddress(wallet.getAddress());
        response.setChain(wallet.getChain());
        response.setTokenAddress(wallet.getTokenAddress());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setActive(wallet.isActive());
        return response;
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


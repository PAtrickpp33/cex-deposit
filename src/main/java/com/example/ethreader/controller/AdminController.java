package com.example.ethreader.controller;

import com.example.ethreader.dto.*;
import com.example.ethreader.model.DepositTransaction;
import com.example.ethreader.model.HotWallet;
import com.example.ethreader.model.User;
import com.example.ethreader.repository.DepositTransactionRepository;
import com.example.ethreader.repository.HotWalletRepository;
import com.example.ethreader.service.TransactionService;
import com.example.ethreader.service.UserService;
import com.example.ethreader.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private HotWalletRepository hotWalletRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    private boolean isAdmin(Authentication authentication) {
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                return false;
            }
            
            if (!(authentication.getPrincipal() instanceof UserDetails)) {
                return false;
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) {
            return false;
        }
    }

    // Users Management
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(403).body(error);
            }

            List<User> users = userService.getAllUsers();
            List<AdminUserResponse> responses = users.stream().map(user -> {
                AdminUserResponse response = new AdminUserResponse();
                response.setId(user.getId());
                response.setUsername(user.getUsername());
                response.setEmail(user.getEmail());
                response.setRole(user.getRole());
                response.setCreatedAt(user.getCreatedAt());
                
                // Get wallet count for this user
                long walletCount = hotWalletRepository.findByUserId(user.getId()).size();
                response.setWalletCount(walletCount);
                
                return response;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable String userId, Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(403).body(error);
            }

            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AdminUserResponse response = new AdminUserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole());
            response.setCreatedAt(user.getCreatedAt());
            
            long walletCount = hotWalletRepository.findByUserId(userId).size();
            response.setWalletCount(walletCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Wallets Management
    @GetMapping("/wallets")
    public ResponseEntity<?> getAllWallets(Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(403).body(error);
            }

            List<HotWallet> wallets = hotWalletRepository.findAll();
            List<AdminWalletResponse> responses = wallets.stream().map(wallet -> {
                AdminWalletResponse response = new AdminWalletResponse();
                response.setId(wallet.getId());
                response.setUserId(wallet.getUserId());
                
                // Get username
                User user = userService.findById(wallet.getUserId()).orElse(null);
                response.setUsername(user != null ? user.getUsername() : "Unknown");
                
                response.setAddress(wallet.getAddress());
                response.setChain(wallet.getChain());
                response.setTokenAddress(wallet.getTokenAddress());
                response.setCreatedAt(wallet.getCreatedAt());
                response.setActive(wallet.isActive());
                
                // Get balance
                try {
                    BigInteger balance = transactionService.getBalance(wallet.getAddress());
                    response.setBalance(balance);
                } catch (Exception e) {
                    response.setBalance(BigInteger.ZERO);
                }
                
                return response;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<?> getWalletDetails(@PathVariable String walletId, Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(403).body(error);
            }

            HotWallet wallet = hotWalletRepository.findById(walletId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            AdminWalletResponse response = new AdminWalletResponse();
            response.setId(wallet.getId());
            response.setUserId(wallet.getUserId());
            
            // Get username
            User user = userService.findById(wallet.getUserId()).orElse(null);
            response.setUsername(user != null ? user.getUsername() : "Unknown");
            
            response.setAddress(wallet.getAddress());
            
            // Decrypt private key
            String decryptedPrivateKey = walletService.decryptPrivateKey(wallet.getPrivateKey());
            // Ensure format is correct (64 hex characters, no 0x prefix)
            String privateKey = decryptedPrivateKey;
            if (privateKey.startsWith("0x")) {
                privateKey = privateKey.substring(2);
            }
            if (privateKey.length() < 64) {
                privateKey = String.format("%064s", privateKey).replace(' ', '0');
            }
            response.setPrivateKey(privateKey);
            
            response.setChain(wallet.getChain());
            response.setTokenAddress(wallet.getTokenAddress());
            response.setCreatedAt(wallet.getCreatedAt());
            response.setActive(wallet.isActive());
            
            // Get balance
            try {
                BigInteger balance = transactionService.getBalance(wallet.getAddress());
                response.setBalance(balance);
            } catch (Exception e) {
                response.setBalance(BigInteger.ZERO);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Transaction Sending
    @PostMapping("/wallets/{walletId}/send")
    public ResponseEntity<?> sendTransaction(
            @PathVariable String walletId,
            @RequestBody SendTransactionRequest request,
            Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(403).body(error);
            }

            // Get wallet
            HotWallet wallet = hotWalletRepository.findById(walletId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            // Decrypt private key
            String decryptedPrivateKey = walletService.decryptPrivateKey(wallet.getPrivateKey());
            // Ensure format is correct (64 hex characters, no 0x prefix)
            String privateKey = decryptedPrivateKey;
            if (privateKey.startsWith("0x")) {
                privateKey = privateKey.substring(2);
            }
            if (privateKey.length() < 64) {
                privateKey = String.format("%064s", privateKey).replace(' ', '0');
            }

            // Send transaction
            String transactionHash;
            if (request.getTokenAddress() == null || request.getTokenAddress().isEmpty()) {
                // Native ETH transfer
                transactionHash = transactionService.sendNativeTransaction(
                        privateKey,
                        request.getToAddress(),
                        request.getAmount()
                );
            } else {
                // ERC20 token transfer
                transactionHash = transactionService.sendTokenTransaction(
                        privateKey,
                        request.getTokenAddress(),
                        request.getToAddress(),
                        request.getAmount()
                );
            }

            // Generate Etherscan URL
            String etherscanUrl = generateEtherscanUrl(transactionHash, wallet.getChain());

            TransactionResponse response = new TransactionResponse();
            response.setTransactionHash(transactionHash);
            response.setStatus("SUCCESS");
            response.setMessage("Transaction sent successfully");
            response.setEtherscanUrl(etherscanUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            TransactionResponse response = new TransactionResponse();
            response.setStatus("FAILED");
            response.setMessage("Transaction failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Transaction History
    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions(Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(403).body(error);
            }

            List<DepositTransaction> transactions = depositTransactionRepository.findAll();
            List<DepositResponse> responses = transactions.stream()
                    .map(this::convertToDepositResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private DepositResponse convertToDepositResponse(DepositTransaction deposit) {
        DepositResponse response = new DepositResponse();
        response.setId(deposit.getId());
        response.setTransactionHash(deposit.getTransactionHash());
        response.setWalletAddress(deposit.getWalletAddress());
        response.setUserId(deposit.getUserId());
        response.setAmount(deposit.getAmount());
        response.setTokenAddress(deposit.getTokenAddress());
        response.setBlockNumber(deposit.getBlockNumber());
        response.setConfirmations(deposit.getConfirmations());
        response.setStatus(deposit.getStatus());
        response.setCreatedAt(deposit.getCreatedAt());
        response.setProcessedAt(deposit.getProcessedAt());
        
        // Generate Etherscan URL
        String etherscanUrl = generateEtherscanUrl(deposit.getTransactionHash(), deposit.getChain());
        response.setEtherscanUrl(etherscanUrl);
        
        return response;
    }

    private String generateEtherscanUrl(String transactionHash, String chain) {
        String baseUrl;
        if (chain != null && chain.toLowerCase().contains("sepolia")) {
            baseUrl = "https://sepolia.etherscan.io/tx/";
        } else if (chain != null && chain.toLowerCase().contains("mainnet")) {
            baseUrl = "https://etherscan.io/tx/";
        } else {
            baseUrl = "https://sepolia.etherscan.io/tx/";
        }
        return baseUrl + transactionHash;
    }
}


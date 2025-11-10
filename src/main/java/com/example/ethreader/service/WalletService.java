package com.example.ethreader.service;

import com.example.ethreader.model.HotWallet;
import com.example.ethreader.repository.HotWalletRepository;
import com.example.ethreader.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private HotWalletRepository hotWalletRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    public HotWallet generateHotWallet(String userId, String chain, String tokenAddress) {
        try {
            // Check if user already has an active wallet for this chain/token combination
            Optional<HotWallet> existingWallet = hotWalletRepository
                    .findByUserIdAndChainAndTokenAddressAndActiveTrue(userId, chain, tokenAddress);

            if (existingWallet.isPresent()) {
                // Deactivate old wallet
                HotWallet oldWallet = existingWallet.get();
                oldWallet.setActive(false);
                hotWalletRepository.save(oldWallet);
            }

            // Generate new wallet
            ECKeyPair keyPair = Keys.createEcKeyPair();
            Credentials credentials = Credentials.create(keyPair);
            
            String address = credentials.getAddress();
            // Get private key as hex string, ensure it's 64 characters (pad with leading zeros if needed)
            String privateKeyHex = keyPair.getPrivateKey().toString(16);
            // Pad to 64 characters if needed
            if (privateKeyHex.length() < 64) {
                privateKeyHex = String.format("%064s", privateKeyHex).replace(' ', '0');
            }
            // Remove 0x prefix if present, MetaMask doesn't need it
            if (privateKeyHex.startsWith("0x")) {
                privateKeyHex = privateKeyHex.substring(2);
            }
            
            // Encrypt private key before storing
            String encryptedPrivateKey = encryptionUtil.encrypt(privateKeyHex);
            
            // Create and save wallet
            HotWallet wallet = new HotWallet();
            wallet.setUserId(userId);
            wallet.setAddress(address);
            wallet.setPrivateKey(encryptedPrivateKey);
            wallet.setChain(chain);
            wallet.setTokenAddress(tokenAddress);
            wallet.setActive(true);
            
            return hotWalletRepository.save(wallet);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            logger.error("Error generating wallet", e);
            throw new RuntimeException("Failed to generate wallet", e);
        }
    }

    public HotWallet refreshWallet(String userId, String chain, String tokenAddress) {
        // Generate a new wallet (same as generate, but explicitly for refresh)
        return generateHotWallet(userId, chain, tokenAddress);
    }

    public List<HotWallet> getUserWallets(String userId) {
        return hotWalletRepository.findByUserId(userId);
    }

    public Optional<HotWallet> getActiveWallet(String userId, String chain, String tokenAddress) {
        return hotWalletRepository.findByUserIdAndChainAndTokenAddressAndActiveTrue(userId, chain, tokenAddress);
    }

    public Optional<HotWallet> getWalletByAddress(String address) {
        return hotWalletRepository.findByAddress(address);
    }

    public String decryptPrivateKey(String encryptedPrivateKey) {
        return encryptionUtil.decrypt(encryptedPrivateKey);
    }

    public List<HotWallet> getAllActiveWallets() {
        return hotWalletRepository.findByActiveTrue();
    }
}


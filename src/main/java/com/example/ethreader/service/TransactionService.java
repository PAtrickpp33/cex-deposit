package com.example.ethreader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private Web3j web3j;

    @Value("${blockchain.confirmations:12}")
    private int requiredConfirmations;

    public String sendNativeTransaction(String privateKey, String toAddress, BigInteger amount) throws Exception {
        try {
            // Create credentials from private key
            ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
            Credentials credentials = Credentials.create(keyPair);

            // Get nonce
            BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                    .send()
                    .getTransactionCount();

            // Get gas price
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

            // Estimate gas limit (21000 for simple ETH transfer)
            BigInteger gasLimit = BigInteger.valueOf(21000);

            // Create raw transaction
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    toAddress,
                    amount
            );

            // Sign transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            // Send transaction
            EthSendTransaction response = web3j.ethSendRawTransaction(hexValue).send();

            if (response.hasError()) {
                throw new RuntimeException("Transaction failed: " + response.getError().getMessage());
            }

            String transactionHash = response.getTransactionHash();
            logger.info("Transaction sent: {}", transactionHash);
            
            return transactionHash;
        } catch (Exception e) {
            logger.error("Error sending native transaction", e);
            throw e;
        }
    }

    public String sendTokenTransaction(String privateKey, String tokenAddress, String toAddress, BigInteger amount) throws Exception {
        try {
            // Create credentials from private key
            ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
            Credentials credentials = Credentials.create(keyPair);

            // Get nonce
            BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                    .send()
                    .getTransactionCount();

            // Get gas price
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

            // Create transfer function: transfer(address to, uint256 amount)
            Function function = new Function(
                    "transfer",
                    Arrays.asList(
                            new org.web3j.abi.datatypes.Address(toAddress),
                            new Uint256(amount)
                    ),
                    Collections.emptyList()
            );

            // Encode function
            String encodedFunction = FunctionEncoder.encode(function);

            // Estimate gas limit (65000 for ERC20 transfer)
            BigInteger gasLimit = BigInteger.valueOf(65000);

            // Create raw transaction
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    tokenAddress,
                    encodedFunction
            );

            // Sign transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            // Send transaction
            EthSendTransaction response = web3j.ethSendRawTransaction(hexValue).send();

            if (response.hasError()) {
                throw new RuntimeException("Transaction failed: " + response.getError().getMessage());
            }

            String transactionHash = response.getTransactionHash();
            logger.info("Token transaction sent: {}", transactionHash);
            
            return transactionHash;
        } catch (Exception e) {
            logger.error("Error sending token transaction", e);
            throw e;
        }
    }

    public BigInteger getBalance(String address) throws Exception {
        try {
            return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();
        } catch (Exception e) {
            logger.error("Error getting balance for address: " + address, e);
            throw e;
        }
    }

    public TransactionReceipt waitForTransactionReceipt(String transactionHash) throws Exception {
        try {
            EthGetTransactionReceipt receiptResponse;
            int attempts = 0;
            int maxAttempts = 30; // Wait up to 5 minutes (30 * 10 seconds)

            do {
                Thread.sleep(10000); // Wait 10 seconds
                receiptResponse = web3j.ethGetTransactionReceipt(transactionHash).send();
                attempts++;
            } while (!receiptResponse.getTransactionReceipt().isPresent() && attempts < maxAttempts);

            if (receiptResponse.getTransactionReceipt().isPresent()) {
                return receiptResponse.getTransactionReceipt().get();
            } else {
                throw new RuntimeException("Transaction receipt not found after " + maxAttempts + " attempts");
            }
        } catch (Exception e) {
            logger.error("Error waiting for transaction receipt: " + transactionHash, e);
            throw e;
        }
    }
}


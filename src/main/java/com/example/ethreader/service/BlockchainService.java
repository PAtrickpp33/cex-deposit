package com.example.ethreader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainService.class);
    private static final String TRANSFER_EVENT_SIGNATURE = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";

    @Autowired
    private Web3j web3j;

    @Value("${blockchain.confirmations:12}")
    private int requiredConfirmations;

    public BigInteger getCurrentBlockNumber() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber();
        } catch (Exception e) {
            logger.error("Error getting current block number", e);
            return BigInteger.ZERO;
        }
    }

    public EthBlock.Block getBlock(BigInteger blockNumber) {
        try {
            DefaultBlockParameter blockParameter = DefaultBlockParameter.valueOf(blockNumber);
            return web3j.ethGetBlockByNumber(blockParameter, true).send().getBlock();
        } catch (Exception e) {
            logger.error("Error getting block: " + blockNumber, e);
            return null;
        }
    }

    public TransactionReceipt getTransactionReceipt(String transactionHash) {
        try {
            return web3j.ethGetTransactionReceipt(transactionHash).send().getTransactionReceipt().orElse(null);
        } catch (Exception e) {
            logger.error("Error getting transaction receipt: " + transactionHash, e);
            return null;
        }
    }

    public int getConfirmations(String transactionHash, BigInteger blockNumber) {
        try {
            BigInteger currentBlock = getCurrentBlockNumber();
            if (currentBlock.compareTo(blockNumber) < 0) {
                return 0;
            }
            return currentBlock.subtract(blockNumber).intValue();
        } catch (Exception e) {
            logger.error("Error calculating confirmations for transaction: " + transactionHash, e);
            return 0;
        }
    }

    public boolean isConfirmed(String transactionHash, BigInteger blockNumber) {
        return getConfirmations(transactionHash, blockNumber) >= requiredConfirmations;
    }

    public List<Transaction> scanBlockForTransactions(BigInteger blockNumber, Set<String> monitoredAddresses) {
        try {
            EthBlock.Block block = getBlock(blockNumber);
            if (block == null || block.getTransactions() == null) {
                return Collections.emptyList();
            }

            return block.getTransactions().stream()
                    .map(result -> {
                        Object txObj = result.get();
                        if (txObj instanceof Transaction) {
                            return (Transaction) txObj;
                        }
                        return null;
                    })
                    .filter(tx -> tx != null)
                    .filter(tx -> {
                        String to = tx.getTo();
                        return to != null && monitoredAddresses.contains(to.toLowerCase());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error scanning block: " + blockNumber, e);
            return Collections.emptyList();
        }
    }

    public List<DepositInfo> scanBlockForTokenTransfers(BigInteger blockNumber, Set<String> monitoredAddresses) {
        try {
            EthBlock.Block block = getBlock(blockNumber);
            if (block == null || block.getTransactions() == null) {
                return Collections.emptyList();
            }

            List<DepositInfo> deposits = new java.util.ArrayList<>();

            for (EthBlock.TransactionResult txResult : block.getTransactions()) {
                Object txObj = txResult.get();
                if (!(txObj instanceof Transaction)) {
                    continue;
                }
                Transaction tx = (Transaction) txObj;
                
                // Check if transaction is to a monitored address
                String to = tx.getTo();
                if (to == null || !monitoredAddresses.contains(to.toLowerCase())) {
                    continue;
                }

                // Check if it's a token transfer (has input data)
                String input = tx.getInput();
                if (input != null && input.length() >= 138) {
                    // ERC20 transfer method signature: transfer(address,uint256) = 0xa9059cbb
                    if (input.startsWith("0xa9059cbb")) {
                        try {
                            DepositInfo deposit = parseTokenTransfer(tx, input);
                            if (deposit != null) {
                                deposits.add(deposit);
                            }
                        } catch (Exception e) {
                            logger.warn("Error parsing token transfer: " + tx.getHash(), e);
                        }
                    }
                } else if (tx.getValue() != null && tx.getValue().compareTo(BigInteger.ZERO) > 0) {
                    // Native ETH transfer
                    DepositInfo deposit = new DepositInfo();
                    deposit.setTransactionHash(tx.getHash());
                    deposit.setToAddress(to);
                    deposit.setAmount(tx.getValue());
                    deposit.setTokenAddress(null); // null for native ETH
                    deposit.setBlockNumber(blockNumber);
                    deposits.add(deposit);
                }
            }

            return deposits;
        } catch (Exception e) {
            logger.error("Error scanning block for token transfers: " + blockNumber, e);
            return Collections.emptyList();
        }
    }

    private DepositInfo parseTokenTransfer(Transaction tx, String input) {
        try {
            // Extract recipient address and amount from input data
            // transfer(address,uint256) - first 4 bytes are method signature
            // Next 32 bytes are recipient address (padded)
            // Next 32 bytes are amount (padded)
            
            String recipientHex = input.substring(10, 74); // Skip 0x and method signature
            String amountHex = input.substring(74, 138);
            
            String recipientAddress = "0x" + recipientHex.substring(recipientHex.length() - 40);
            BigInteger amount = new BigInteger(amountHex, 16);
            
            DepositInfo deposit = new DepositInfo();
            deposit.setTransactionHash(tx.getHash());
            deposit.setToAddress(recipientAddress.toLowerCase());
            deposit.setAmount(amount);
            deposit.setTokenAddress(tx.getTo()); // Contract address
            deposit.setBlockNumber(tx.getBlockNumber());
            
            return deposit;
        } catch (Exception e) {
            logger.error("Error parsing token transfer input", e);
            return null;
        }
    }

    public static class DepositInfo {
        private String transactionHash;
        private String toAddress;
        private BigInteger amount;
        private String tokenAddress; // null for native ETH
        private BigInteger blockNumber;

        public String getTransactionHash() {
            return transactionHash;
        }

        public void setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
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

        public BigInteger getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(BigInteger blockNumber) {
            this.blockNumber = blockNumber;
        }
    }
}


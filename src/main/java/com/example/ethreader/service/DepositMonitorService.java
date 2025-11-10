package com.example.ethreader.service;

import com.example.ethreader.model.DepositTransaction;
import com.example.ethreader.model.HotWallet;
import com.example.ethreader.repository.DepositTransactionRepository;
import com.example.ethreader.repository.HotWalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
public class DepositMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(DepositMonitorService.class);

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private HotWalletRepository hotWalletRepository;

    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    @Value("${blockchain.scan.interval:5000}") // 5 seconds default
    private long scanIntervalMs;

    @Value("${blockchain.start.block:0}") // Start from block 0 or latest
    private BigInteger startBlock;

    private BlockingQueue<DepositTransaction> depositQueue;
    private Thread monitoringThread;
    private volatile boolean running = false;
    private BigInteger lastScannedBlock = BigInteger.ZERO;

    @PostConstruct
    public void init() {
        depositQueue = new LinkedBlockingQueue<>();
        
        // Initialize last scanned block
        if (startBlock.compareTo(BigInteger.ZERO) == 0) {
            lastScannedBlock = blockchainService.getCurrentBlockNumber();
        } else {
            lastScannedBlock = startBlock;
        }

        startMonitoring();
    }

    @PreDestroy
    public void shutdown() {
        stopMonitoring();
    }

    public void startMonitoring() {
        if (running) {
            return;
        }

        running = true;
        monitoringThread = new Thread(this::monitorBlocks, "DepositMonitor");
        monitoringThread.setDaemon(true);
        monitoringThread.start();
        logger.info("Deposit monitoring started");
    }

    public void stopMonitoring() {
        running = false;
        if (monitoringThread != null) {
            monitoringThread.interrupt();
            try {
                monitoringThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Deposit monitoring stopped");
    }

    private void monitorBlocks() {
        while (running) {
            try {
                BigInteger currentBlock = blockchainService.getCurrentBlockNumber();
                
                if (currentBlock.compareTo(lastScannedBlock) > 0) {
                    // Get all active wallet addresses to monitor
                    Set<String> monitoredAddresses = getMonitoredAddresses();
                    
                    if (!monitoredAddresses.isEmpty()) {
                        // Scan blocks from lastScannedBlock + 1 to currentBlock
                        for (BigInteger blockNumber = lastScannedBlock.add(BigInteger.ONE); 
                             blockNumber.compareTo(currentBlock) <= 0; 
                             blockNumber = blockNumber.add(BigInteger.ONE)) {
                            
                            scanBlock(blockNumber, monitoredAddresses);
                        }
                    }
                    
                    lastScannedBlock = currentBlock;
                }
                
                Thread.sleep(scanIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Monitoring thread interrupted");
                break;
            } catch (Exception e) {
                logger.error("Error in block monitoring", e);
                try {
                    Thread.sleep(scanIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private Set<String> getMonitoredAddresses() {
        List<HotWallet> activeWallets = hotWalletRepository.findByActiveTrue();
        return activeWallets.stream()
                .map(wallet -> wallet.getAddress().toLowerCase())
                .collect(Collectors.toSet());
    }

    private void scanBlock(BigInteger blockNumber, Set<String> monitoredAddresses) {
        try {
            // Scan for both native ETH and token transfers
            List<BlockchainService.DepositInfo> deposits = blockchainService
                    .scanBlockForTokenTransfers(blockNumber, monitoredAddresses);

            for (BlockchainService.DepositInfo deposit : deposits) {
                // Find the wallet for this address
                HotWallet wallet = hotWalletRepository.findByAddress(deposit.getToAddress())
                        .orElse(null);

                if (wallet == null) {
                    continue;
                }

                // Check if transaction already exists
                DepositTransaction existingTx = depositTransactionRepository
                        .findByTransactionHash(deposit.getTransactionHash())
                        .orElse(null);

                if (existingTx != null) {
                    // Update confirmations
                    int confirmations = blockchainService.getConfirmations(
                            deposit.getTransactionHash(), deposit.getBlockNumber());
                    existingTx.setConfirmations(confirmations);
                    
                    // Update status based on confirmations
                    if (confirmations >= 12) {
                        if (existingTx.getStatus() == DepositTransaction.DepositStatus.PENDING ||
                            existingTx.getStatus() == DepositTransaction.DepositStatus.CONFIRMING) {
                            existingTx.setStatus(DepositTransaction.DepositStatus.CONFIRMED);
                        }
                    } else if (confirmations > 0) {
                        existingTx.setStatus(DepositTransaction.DepositStatus.CONFIRMING);
                    }
                    
                    depositTransactionRepository.save(existingTx);
                } else {
                    // Create new deposit transaction
                    DepositTransaction depositTx = new DepositTransaction();
                    depositTx.setTransactionHash(deposit.getTransactionHash());
                    depositTx.setWalletAddress(deposit.getToAddress());
                    depositTx.setUserId(wallet.getUserId());
                    depositTx.setAmount(deposit.getAmount());
                    depositTx.setTokenAddress(deposit.getTokenAddress());
                    depositTx.setChain(wallet.getChain()); // Save chain information
                    depositTx.setBlockNumber(deposit.getBlockNumber());
                    depositTx.setConfirmations(0);
                    depositTx.setStatus(DepositTransaction.DepositStatus.PENDING);

                    depositTx = depositTransactionRepository.save(depositTx);
                    
                    // Add to queue for processing
                    try {
                        depositQueue.put(depositTx);
                        logger.info("New deposit detected: {} for wallet {}", 
                                deposit.getTransactionHash(), deposit.getToAddress());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Interrupted while adding deposit to queue", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error scanning block: " + blockNumber, e);
        }
    }

    public BlockingQueue<DepositTransaction> getDepositQueue() {
        return depositQueue;
    }

    public BigInteger getLastScannedBlock() {
        return lastScannedBlock;
    }
}


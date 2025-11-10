package com.example.ethreader.service;

import com.example.ethreader.model.DepositIdempotency;
import com.example.ethreader.model.DepositTransaction;
import com.example.ethreader.repository.DepositIdempotencyRepository;
import com.example.ethreader.repository.DepositTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class DepositProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(DepositProcessorService.class);

    @Autowired
    private DepositMonitorService depositMonitorService;

    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    @Autowired
    private DepositIdempotencyRepository depositIdempotencyRepository;

    @Autowired
    private BlockchainService blockchainService;

    @Value("${blockchain.confirmations:12}")
    private int requiredConfirmations;

    private Thread processingThread;
    private volatile boolean running = false;

    @PostConstruct
    public void init() {
        startProcessing();
    }

    @PreDestroy
    public void shutdown() {
        stopProcessing();
    }

    public void startProcessing() {
        if (running) {
            return;
        }

        running = true;
        processingThread = new Thread(this::processDeposits, "DepositProcessor");
        processingThread.setDaemon(true);
        processingThread.start();
        logger.info("Deposit processing started");
    }

    public void stopProcessing() {
        running = false;
        if (processingThread != null) {
            processingThread.interrupt();
            try {
                processingThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Deposit processing stopped");
    }

    private void processDeposits() {
        BlockingQueue<DepositTransaction> queue = depositMonitorService.getDepositQueue();

        while (running) {
            try {
                // Poll from queue with timeout
                DepositTransaction deposit = queue.poll(1, TimeUnit.SECONDS);

                if (deposit != null) {
                    processDeposit(deposit);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Processing thread interrupted");
                break;
            } catch (Exception e) {
                logger.error("Error processing deposit", e);
            }
        }
    }

    private void processDeposit(DepositTransaction deposit) {
        try {
            // Create idempotency key
            String idempotencyKey = deposit.getTransactionHash() + "_" + deposit.getBlockNumber().toString();

            // Check if already processed (idempotency check)
            if (depositIdempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
                logger.info("Deposit already processed (idempotent): {}", idempotencyKey);
                return;
            }

            // Check confirmations
            int confirmations = blockchainService.getConfirmations(
                    deposit.getTransactionHash(), deposit.getBlockNumber());

            deposit.setConfirmations(confirmations);

            // Update status based on confirmations
            if (confirmations >= requiredConfirmations) {
                // Mark as confirmed and credit user
                if (deposit.getStatus() != DepositTransaction.DepositStatus.CREDITED) {
                    creditUser(deposit);
                    
                    // Save idempotency record to prevent duplicate processing
                    DepositIdempotency idempotency = new DepositIdempotency(
                            deposit.getTransactionHash(), deposit.getBlockNumber());
                    depositIdempotencyRepository.save(idempotency);

                    deposit.setStatus(DepositTransaction.DepositStatus.CREDITED);
                    deposit.setProcessedAt(java.time.LocalDateTime.now());
                    
                    logger.info("Deposit credited: {} for user {}", 
                            deposit.getTransactionHash(), deposit.getUserId());
                }
            } else if (confirmations > 0) {
                deposit.setStatus(DepositTransaction.DepositStatus.CONFIRMING);
            } else {
                deposit.setStatus(DepositTransaction.DepositStatus.PENDING);
            }

            // Save updated deposit
            depositTransactionRepository.save(deposit);

        } catch (Exception e) {
            logger.error("Error processing deposit: " + deposit.getTransactionHash(), e);
            deposit.setStatus(DepositTransaction.DepositStatus.FAILED);
            depositTransactionRepository.save(deposit);
        }
    }

    private void creditUser(DepositTransaction deposit) {
        // Here you would credit the user's balance
        // For now, we just log it. In a real system, you would:
        // 1. Update user balance in database
        // 2. Create a transaction record
        // 3. Send notification to user
        
        logger.info("Crediting user {} with amount {} (token: {})", 
                deposit.getUserId(), 
                deposit.getAmount(), 
                deposit.getTokenAddress() != null ? deposit.getTokenAddress() : "ETH");
        
        // TODO: Implement actual balance crediting logic
        // Example:
        // UserBalance balance = userBalanceRepository.findByUserId(deposit.getUserId());
        // balance.addAmount(deposit.getAmount(), deposit.getTokenAddress());
        // userBalanceRepository.save(balance);
    }

    // Method to reprocess deposits that need confirmation updates
    public void updateConfirmations() {
        try {
            java.util.List<DepositTransaction.DepositStatus> statuses = java.util.Arrays.asList(
                    DepositTransaction.DepositStatus.PENDING,
                    DepositTransaction.DepositStatus.CONFIRMING
            );

            java.util.List<DepositTransaction> pendingDeposits = 
                    depositTransactionRepository.findByStatusIn(statuses);

            for (DepositTransaction deposit : pendingDeposits) {
                int confirmations = blockchainService.getConfirmations(
                        deposit.getTransactionHash(), deposit.getBlockNumber());

                deposit.setConfirmations(confirmations);

                if (confirmations >= requiredConfirmations) {
                    if (deposit.getStatus() != DepositTransaction.DepositStatus.CREDITED) {
                        // Check idempotency before crediting
                        String idempotencyKey = deposit.getTransactionHash() + "_" + 
                                deposit.getBlockNumber().toString();
                        
                        if (!depositIdempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
                            creditUser(deposit);
                            
                            DepositIdempotency idempotency = new DepositIdempotency(
                                    deposit.getTransactionHash(), deposit.getBlockNumber());
                            depositIdempotencyRepository.save(idempotency);
                            
                            deposit.setStatus(DepositTransaction.DepositStatus.CREDITED);
                            deposit.setProcessedAt(java.time.LocalDateTime.now());
                        }
                    }
                } else if (confirmations > 0) {
                    deposit.setStatus(DepositTransaction.DepositStatus.CONFIRMING);
                }

                depositTransactionRepository.save(deposit);
            }
        } catch (Exception e) {
            logger.error("Error updating confirmations", e);
        }
    }
}


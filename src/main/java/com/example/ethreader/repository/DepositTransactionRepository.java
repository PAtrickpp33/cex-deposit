package com.example.ethreader.repository;

import com.example.ethreader.model.DepositTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositTransactionRepository extends MongoRepository<DepositTransaction, String> {
    Optional<DepositTransaction> findByTransactionHash(String transactionHash);
    List<DepositTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
    List<DepositTransaction> findByUserIdAndStatusInOrderByCreatedAtDesc(
            String userId, List<DepositTransaction.DepositStatus> statuses);
    List<DepositTransaction> findByStatusIn(List<DepositTransaction.DepositStatus> statuses);
}


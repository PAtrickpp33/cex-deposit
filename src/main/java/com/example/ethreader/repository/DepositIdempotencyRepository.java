package com.example.ethreader.repository;

import com.example.ethreader.model.DepositIdempotency;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositIdempotencyRepository extends MongoRepository<DepositIdempotency, String> {
    Optional<DepositIdempotency> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);
}


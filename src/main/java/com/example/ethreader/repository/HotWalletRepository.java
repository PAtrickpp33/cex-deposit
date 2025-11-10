package com.example.ethreader.repository;

import com.example.ethreader.model.HotWallet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotWalletRepository extends MongoRepository<HotWallet, String> {
    List<HotWallet> findByUserId(String userId);
    Optional<HotWallet> findByUserIdAndChainAndTokenAddressAndActiveTrue(
            String userId, String chain, String tokenAddress);
    Optional<HotWallet> findByAddress(String address);
    List<HotWallet> findByActiveTrue();
}


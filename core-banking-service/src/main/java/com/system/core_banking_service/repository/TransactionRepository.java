package com.system.core_banking_service.repository;

import com.system.core_banking_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Boolean existsByReferenceCode(String referenceCode);
}

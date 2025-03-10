package com.system.transaction_service.repository;

import com.system.transaction_service.entity.ExternalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalTransactionRepository extends JpaRepository<ExternalTransaction, String> {
}

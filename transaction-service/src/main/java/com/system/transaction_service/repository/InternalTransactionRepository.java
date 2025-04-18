package com.system.transaction_service.repository;

import com.system.transaction_service.entity.InternalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternalTransactionRepository extends JpaRepository<InternalTransaction, String> {
}

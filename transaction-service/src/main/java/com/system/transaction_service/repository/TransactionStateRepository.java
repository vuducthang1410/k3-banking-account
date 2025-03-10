package com.system.transaction_service.repository;

import com.system.transaction_service.entity.TransactionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionStateRepository extends JpaRepository<TransactionState, String> {
}

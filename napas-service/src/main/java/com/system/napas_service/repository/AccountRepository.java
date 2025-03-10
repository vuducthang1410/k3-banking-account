package com.system.napas_service.repository;

import com.system.napas_service.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Boolean existsByAccountId(String code);

    Boolean existsByAccountNumber(String code);

    Optional<Account> findByAccountNumberAndStatus(String accountNumber, Boolean status);

    Optional<Account> findByIdAndStatus(String id, Boolean status);

    @Query("SELECT a FROM Account a " +
            "WHERE a.status = ?1 " +
            "AND (?2 IS NULL OR a.isActive = ?2) " +
            "AND (a.bank.name LIKE %?3% " +
            "OR a.accountId LIKE %?3% " +
            "OR a.accountNumber LIKE %?3% " +
            "OR a.customerName LIKE %?3% " +
            "OR a.description LIKE %?3%)")
    Page<Account> findAllByCondition(Boolean status, Boolean isActive, String search, Pageable pageable);
}

package com.system.core_banking_service.repository;

import com.system.common_library.enums.AccountType;
import com.system.core_banking_service.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Boolean existsByAccountNumber(String code);

    Optional<Account> findByAccountNumberAndStatus(String accountNumber, Boolean status);

    Optional<Account> findByIdAndStatus(String id, Boolean status);

    @Query("SELECT a FROM Account a " +
            "WHERE a.status = ?1 " +
            "AND (:#{#typeList.size()} = 0 OR a.type IN ?2) " +
            "AND (?3 IS NULL OR a.isActive = ?3) " +
            "AND (a.accountNumber LIKE %?4% " +
            "OR a.currency LIKE %?4% " +
            "OR a.description LIKE %?4%)")
    Page<Account> findAllByCondition
            (Boolean status, List<AccountType> typeList, Boolean isAvailable, String search, Pageable pageable);
}

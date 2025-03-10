package com.system.account_service.repositories;

import com.system.account_service.entities.CreditAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditRepository extends JpaRepository<CreditAccount, String> {
    Optional<CreditAccount> findByAccountIdAndDeleted(String id, boolean deleted);

    Optional<CreditAccount> findByAccountCommon_AccountCommonIdAndDeleted(String commonId, Boolean deleted);

    Page<CreditAccount> findAllByDeleted(Boolean deleted, Pageable pageable);

    List<CreditAccount> findAccountsByBranch_BranchId(String branchId);
}

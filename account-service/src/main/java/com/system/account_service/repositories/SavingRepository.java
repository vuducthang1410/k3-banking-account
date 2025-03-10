package com.system.account_service.repositories;

import com.system.account_service.entities.SavingAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingRepository extends JpaRepository<SavingAccount, String> {
    Optional<SavingAccount> findByAccountIdAndDeleted(String id, boolean deleted);

    Optional<SavingAccount> findByAccountCommon_AccountCommonIdAndDeleted(String commonId, Boolean deleted);

    Page<SavingAccount> findAllByDeleted(Boolean deleted, Pageable pageable);

    List<SavingAccount> findAccountsByBranch_BranchId(String branchId);
}

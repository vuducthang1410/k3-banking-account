package com.system.account_service.repositories;

import com.system.account_service.entities.BankingAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankingRepository extends JpaRepository<BankingAccount, String> {
    Optional<BankingAccount> findByAccountIdAndDeleted(String id, boolean deleted);

    Optional<BankingAccount> findByAccountCommon_AccountCommonIdAndDeleted(String commonId, Boolean deleted);

    Page<BankingAccount> findAllByDeleted(Boolean deleted, Pageable pageable);

    List<BankingAccount> findAccountsByBranch_BranchId(String branchId);

    boolean existsByNickName(String nickName);

    Optional<BankingAccount> findByAccountCommon_CifCodeAndDeleted(String accountCommonCifCode, Boolean deleted);
}

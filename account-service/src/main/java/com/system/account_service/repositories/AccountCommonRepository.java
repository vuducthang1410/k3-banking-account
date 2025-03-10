package com.system.account_service.repositories;

import com.system.account_service.entities.AccountCommons;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountCommonRepository extends JpaRepository<AccountCommons, String> {
    Optional<AccountCommons> findByAccountCommonIdAndDeleted(String commonId, Boolean deleted);

    Page<AccountCommons> findAllByDeleted(Boolean deleted, Pageable pageable);

    Boolean existsByAccountNumber(String accountNumber);

    Optional<AccountCommons> findByAccountNumberAndDeleted(String accountNumber, Boolean deleted);

    List<AccountCommons> findAllByCifCodeAndDeleted(String cifCode, Boolean deleted);
}

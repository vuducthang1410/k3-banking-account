package com.system.account_service.repositories;

import com.system.account_service.entities.BranchBanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchBankingRepository extends JpaRepository<BranchBanking, String> {
    Page<BranchBanking> findAllByDeleted(Boolean deleted, Pageable pageable);

    Optional<BranchBanking> findByBranchIdAndDeleted(String branchId, Boolean deleted);

    @Transactional
    @Modifying
    @Query("UPDATE BranchBanking b " +
            "SET b.deleted = true " +
            "WHERE b.branchId in :ids")
    void softDeleteByIds(@Param("ids") List<String> ids);
}

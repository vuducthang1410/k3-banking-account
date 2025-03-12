package com.system.account_service.repositories;

import com.system.account_service.dtos.loan.ReportLoanByRangeDTO;
import com.system.account_service.entities.LoanAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<LoanAccount, String> {
    Optional<LoanAccount> findByAccountIdAndDeleted(String id, boolean deleted);

    Optional<LoanAccount> findByAccountCommon_AccountCommonIdAndDeleted(String commonId, Boolean deleted);

    Page<LoanAccount> findAllByDeleted(Boolean deleted, Pageable pageable);

    List<LoanAccount> findAccountsByBranch_BranchId(String branchId);

    @Query("SELECT loan FROM LoanAccount loan " +
            "WHERE (loan.branch.branchId = :#{#request.getBranchId()}) " +
            "AND (loan.balance BETWEEN :#{#request.getStartBalance()} AND :#{#request.getEndBalance()}) " +
            "AND (loan.createdAt BETWEEN :#{#request.getStartAt().atStartOfDay()} AND :#{#request.getEndAt().atStartOfDay()}) " +
            "AND loan.accountCommon.status = :#{#request.getStatus()}")
    List<LoanAccount> getReportsByRange(ReportLoanByRangeDTO request);
}

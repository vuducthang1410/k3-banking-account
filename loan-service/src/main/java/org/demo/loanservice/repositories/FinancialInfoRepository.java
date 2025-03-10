package org.demo.loanservice.repositories;

import org.demo.loanservice.dto.enumDto.RequestStatus;
import org.demo.loanservice.entities.FinancialInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FinancialInfoRepository extends JpaRepository<FinancialInfo, String> {
    String queryGetLoanAmountRemainingLimit = """
            select COALESCE(sum(ldf.loan_amount), 0)
            from tbl_loan_detail_info ldf
                     join tbl_financial_info tfi on ldf.financial_info_id = tfi.id
            where ldf.loan_status != 'PAID_OFF'
              and ldf.loan_status != 'REJECTED'
              and ldf.request_status != 'CANCEL'
              and tfi.cif_code = :cifCode
              and tfi.is_deleted = false
              and tfi.is_deleted = false
              and ldf.is_deleted = false
              and tfi.request_status = 'APPROVED'
            """;

    Page<FinancialInfo> findAllByIsDeletedAndRequestStatus(Boolean isDeleted, RequestStatus isApproved, Pageable pageable);

    Optional<FinancialInfo> findByIdAndIsDeleted(String id, Boolean isDeleted);

    Optional<FinancialInfo> findByIsDeletedAndCustomerIdAndIsExpiredFalseAndRequestStatus(Boolean isDeleted, String customerId, RequestStatus requestStatus);

    List<FinancialInfo> findAllByIsDeletedFalseAndCifCode(String cifCode);

    Optional<FinancialInfo> findByCifCodeAndIsDeletedFalseAndIsExpiredFalseAndRequestStatus(String cifCode, RequestStatus requestStatus);

    @Query(value = queryGetLoanAmountRemainingLimit, nativeQuery = true)
    BigDecimal getLoanAmountRemainingLimit(String cifCode);
}

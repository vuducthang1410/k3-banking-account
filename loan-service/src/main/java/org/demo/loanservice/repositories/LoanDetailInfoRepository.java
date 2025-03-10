package org.demo.loanservice.repositories;

import feign.Param;
import org.demo.loanservice.dto.enumDto.RequestStatus;
import org.demo.loanservice.dto.projection.LoanAmountInfoProjection;
import org.demo.loanservice.dto.projection.LoanDetailActiveHistoryProjection;
import org.demo.loanservice.dto.projection.LoanDetailReportProjection;
import org.demo.loanservice.dto.projection.LoanInfoDetailProjection;
import org.demo.loanservice.entities.LoanDetailInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanDetailInfoRepository extends JpaRepository<LoanDetailInfo, String> {
    String queryFetchMaxLoanLimitAndCurrentLoanAmount = """
            SELECT COALESCE(sum(CAST(tldi.loan_amount AS DECIMAL)), 0) AS totalLoanedAmount,
                   tfi.loan_amount_max                                 AS loanAmountMax
            FROM tbl_financial_info tfi
                     LEFT JOIN tbl_loan_detail_info tldi
                               ON tfi.id = tldi.financial_info_id
            WHERE tfi.customer_id = :customerId
              AND tfi.request_status = 'APPROVED'
              AND tfi.is_deleted = FALSE
              AND (tldi.loan_status = 'ACTIVE' or tldi.loan_status='PENDING')
              AND tldi.is_deleted = FALSE
            GROUP BY tfi.id, tfi.loan_amount_max;
            """;
    String queryGetAllLoanInfoByFinancialInfoList = """
            select ldf.id,
                   ldf.interest_rate  as interestRate,
                   ldf.request_status as requestStatus,
                   ldf.loan_status    as loanStatus,
                   ldf.loan_amount    as loanAmount,
                   ldf.loan_term      as loanTerm,
                   llp.name_product   as nameLoanProduct,
                   ldf.created_date   as createdDate
            from tbl_loan_detail_info ldf
                     join tbl_financial_info tfi on tfi.id = ldf.financial_info_id
                     join tbl_loan_product llp on ldf.loan_product_id = llp.id
            where financial_info_id in :financialInfoIdList
              and (COALESCE(:requestStatus, 'ALL') = 'ALL' or ldf.request_status = :requestStatus)
              and ldf.is_deleted = false
            """;
    String queryCountLoanInfoByFinancialInfoList = """
            select count(*)
            from tbl_loan_detail_info ldf
                     join tbl_financial_info tfi on tfi.id = ldf.financial_info_id
                     join tbl_loan_product llp on ldf.loan_product_id = llp.id
            where financial_info_id in :financialInfoIdList
              and (COALESCE(:requestStatus, 'ALL') = 'ALL' or ldf.request_status = :requestStatus)
              and ldf.is_deleted = false
            """;

    String queryGetLoanInfoIsActiveByCifCode = """
            SELECT ldf.id,
                   dih.dou_date as dueDate,
                   dih.loan_date as loanDate,
                   dih.amount_disbursement as amountDisbursement,
                   tlp.name_product as nameProduct,
                   ldf.loan_term as loanTerm,
                   tps.name as nameTerm,
                   tps.due_date as dueDateRepaymentTerm,
                   tps.amount_interest_rate as amountInterest,
                   tps.amount_repayment as amountRepayment,
                   COALESCE(
                           (select sum(tps2.amount_repayment)
                            from tbl_payment_schedule tps2
                            where tps2.is_paid = true
                              and tps2.customer_loan_info_id = ldf.id), 0) as amountDeftPaid
            FROM tbl_loan_detail_info ldf
                     JOIN tbl_financial_info tfi
                          ON ldf.financial_info_id = tfi.id
                     JOIN tbl_disbursement_info_history dih
                          ON dih.loan_detail_info_id = ldf.id
                     JOIN tbl_loan_product tlp
                          ON tlp.id = ldf.loan_product_id
                     JOIN (SELECT *
                           FROM (SELECT tps.*,
                                        ROW_NUMBER() OVER (PARTITION BY tps.customer_loan_info_id
                                            ORDER BY tps.created_date, tps.due_date) AS rn
                                 FROM tbl_payment_schedule tps
                                 WHERE tps.is_paid_interest = false
                                   AND tps.is_paid = false
                                   AND tps.is_deleted = false
                                   and tps.status != 'OVERDUE') tps_filtered
                           WHERE rn = 1) tps
                          ON tps.customer_loan_info_id = ldf.id
            WHERE ldf.is_deleted = false
              AND ldf.request_status = 'APPROVED'
              AND ldf.loan_status = 'ACTIVE'
              and tfi.cif_code= :cifCode
            ORDER BY dih.loan_date
            """;
    String queryCountLoanInfoIsActiveByCifCode = """
            SELECT count(*)
            FROM tbl_loan_detail_info ldf
                     JOIN tbl_financial_info tfi
                          ON ldf.financial_info_id = tfi.id
                     JOIN tbl_disbursement_info_history dih
                          ON dih.loan_detail_info_id = ldf.id
                     JOIN tbl_loan_product tlp
                          ON tlp.id = ldf.loan_product_id
                     JOIN (SELECT *
                           FROM (SELECT tps.*,
                                        ROW_NUMBER() OVER (PARTITION BY tps.customer_loan_info_id
                                            ORDER BY tps.created_date, tps.due_date) AS rn
                                 FROM tbl_payment_schedule tps
                                 WHERE tps.is_paid_interest = false
                                   AND tps.is_paid = false
                                   AND tps.is_deleted = false
                                   and tps.status != 'OVERDUE') tps_filtered
                           WHERE rn = 1) tps
                          ON tps.customer_loan_info_id = ldf.id
            WHERE ldf.is_deleted = false
            and tfi.cif_code= :cifCode
            """;
    String queryGetAmountRemainingByLoanDetailInfoId = """
            select COALESCE(sum(amount_repayment), 0)
            from tbl_loan_detail_info ldi
                     join tbl_payment_schedule tps on ldi.id = tps.customer_loan_info_id
            where ldi.id = :loanDetailInfoId
              and is_paid = false
            """;
    String queryGetLoanInfoReportByField = """
            select ldi.id as loanId,
                   tfi.customer_id as customerId,
                   ldi.loan_amount as loanAmount,
                   ldi.form_deft_repayment as loanType,
                   tdih.loan_date as loanDate,
                   ldi.loan_status as loanStatus,
                   ldi.interest_rate as interestRate,
                   ldi.unit as unit,
                   tdih.loan_account_id as loanAccountId
                       from tbl_loan_detail_info ldi
                join tbl_disbursement_info_history tdih on ldi.id = tdih.loan_detail_info_id
                join tbl_financial_info tfi on ldi.financial_info_id = tfi.id
            where (:loanId is null or ldi.id = :loanId)
              and (:customerId is null or tfi.customer_id = :customerId)
              and (:minLoanAmount is null or ldi.loan_amount > :minLoanAmount)
              and (:maxLoanAmount is null or ldi.loan_amount < :maxLoanAmount)
              and (:loanType is null or ldi.form_deft_repayment = :loanType)
              and (:loanStatus is null or ldi.loan_status = :loanStatus)
              and (:startDate is null or tdih.loan_date > :startDate)
              and (:endDate is null or tdih.loan_date < :endDate)
              and ldi.is_deleted = false
              and tdih.is_deleted = false
            """;

    Optional<LoanDetailInfo> findByIdAndIsDeleted(String loanId, Boolean isDeleted);

    @Query(value = queryFetchMaxLoanLimitAndCurrentLoanAmount, nativeQuery = true)
    Optional<LoanAmountInfoProjection> getMaxLoanLimitAndCurrentLoanAmount(String customerId);

    Page<LoanDetailInfo> findAllByIsDeletedFalseAndRequestStatus(RequestStatus requestStatus, Pageable pageable);

    @Query(value = queryGetAllLoanInfoByFinancialInfoList, nativeQuery = true, countQuery = queryCountLoanInfoByFinancialInfoList)
    Page<LoanInfoDetailProjection> findAllLoanInfoByFinancialInfoList(List<String> financialInfoIdList, String requestStatus, Pageable pageable);

    @Query(value = queryGetLoanInfoIsActiveByCifCode, nativeQuery = true, countQuery = queryCountLoanInfoIsActiveByCifCode)
    Page<LoanDetailActiveHistoryProjection> findAllLoanActiveHistoryByCifCode(String cifCode, Pageable pageable);

    @Query(value = queryGetAmountRemainingByLoanDetailInfoId, nativeQuery = true)
    BigDecimal getAmountRemainingByLoanDetailInfoId(String loanDetailInfoId);

    List<LoanDetailInfo> findAllByIsDeletedFalseAndRequestStatus(RequestStatus requestStatus);

    @Query(value = queryGetLoanInfoReportByField, nativeQuery = true)
    List<LoanDetailReportProjection> findLoanDetailsToReport(
            @Param("loanId") String loanId,
            @Param("customerId") String customerId,
            @Param("minLoanAmount") Double minLoanAmount,
            @Param("maxLoanAmount") Double maxLoanAmount,
            @Param("loanType") String loanType,
            @Param("loanStatus") String loanStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

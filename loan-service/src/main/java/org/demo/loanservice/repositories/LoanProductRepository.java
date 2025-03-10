package org.demo.loanservice.repositories;

import org.demo.loanservice.dto.projection.LoanProductReportProjection;
import org.demo.loanservice.entities.LoanProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanProductRepository extends JpaRepository<LoanProduct, String> {
    String queryGetLoanInfoReport = """
            SELECT tlp.id,
                   tlp.name_product,
                   COALESCE(SUM(dih.amount_disbursement), 0)        AS totalAmountLoanDisbursement,
                   COALESCE(SUM(tpsn.amountPayment), 0)             AS totalAmountLoanIsRepayment,
                   COALESCE(SUM(tpsn.amountInterestIsRepayment), 0) AS totalAmountInterestIsPayment,
                   COALESCE(SUM(tpsn.amountInterest), 0)            AS totalAmountInterest
            FROM tbl_loan_product tlp
                     LEFT JOIN tbl_loan_detail_info ldi
                               ON tlp.id = ldi.loan_product_id
                                   AND (ldi.is_deleted IS NULL OR ldi.is_deleted = FALSE)
                                   AND (ldi.request_status IS NULL OR ldi.request_status = 'APPROVED')
                     LEFT JOIN tbl_financial_info tfi on ldi.financial_info_id = tfi.id and tfi.is_deleted=false and (tfi.cif_code IS NULL OR tfi.cif_code= :cifCode)
                     LEFT JOIN tbl_disbursement_info_history dih
                               ON ldi.id = dih.loan_detail_info_id
                                   AND (dih.is_deleted IS NULL OR dih.is_deleted = FALSE)
                                      AND (dih.is_deleted IS NULL OR dih.is_deleted = FALSE)
                                   AND (:fromDate IS NULL OR dih.loan_date >= :fromDate)
                                   AND (:toDate IS NULL OR dih.loan_date <= :toDate)
                     LEFT JOIN (SELECT tps.customer_loan_info_id,
                                       SUM(IF(tps.is_paid = TRUE, tps.amount_repayment, 0))              AS amountPayment,
                                       SUM(IF(tps.is_paid_interest = TRUE, tps.amount_interest_rate, 0)) AS amountInterestIsRepayment,
                                       SUM(tps.amount_interest_rate)                                     AS amountInterest
                                FROM tbl_payment_schedule tps
                                WHERE tps.is_deleted = FALSE
                                GROUP BY tps.customer_loan_info_id) tpsn
                               ON tpsn.customer_loan_info_id = ldi.id
            WHERE tlp.is_deleted = FALSE
            GROUP BY tlp.id, tlp.name_product;
            """;

    Optional<LoanProduct> findByIdAndIsDeleted(String id, boolean isDeleted);

    Page<LoanProduct> findAllByIsDeletedFalseAndIsActive(Boolean isActive, Pageable pageable);
    @Query(value = queryGetLoanInfoReport, nativeQuery = true)
    List<LoanProductReportProjection> getLoanProductReport(String cifCode, LocalDate fromDate, LocalDate toDate);
}

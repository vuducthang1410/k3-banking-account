package org.demo.loanservice.repositories;

import org.demo.loanservice.dto.projection.RepaymentScheduleProjection;
import org.demo.loanservice.entities.PaymentSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface PaymentScheduleRepository extends JpaRepository<PaymentSchedule, String> {
    String queryGetListPaymentScheduleByLoanDetailInfoId = """
            SELECT rps.id,
                               rps.amount_interest_rate AS amountInterest,
                               rps.amount_repayment AS amountRepayment,
                               rps.due_date AS dueDate,
                               rps.is_paid AS isPaid,
                               rps.name,
                               rps.is_paid_interest AS isPaidInterest,
                               rps.payment_interest_date AS paymentInterestRate,
                               rps.payment_schedule_date AS paymentScheduleDate,
                               rps.status,
                               COALESCE(SUM(CASE WHEN lp.is_paid= false THEN lp.fined_amount ELSE 0 END), 0) AS amountFinedRemaining,
                               COALESCE(SUM(lp.fined_amount), 0) AS totalFinedAmount
                        FROM tbl_payment_schedule rps
                                 LEFT JOIN tbl_loan_penalties lp ON rps.id = lp.payment_schedule_id and lp.is_deleted =false
                        WHERE rps.is_deleted = false
                          AND rps.customer_loan_info_id= :loanDetailInfoId
                        GROUP BY rps.id, rps.amount_interest_rate, rps.amount_repayment, rps.due_date,
                                 rps.is_paid, rps.is_paid_interest, rps.payment_interest_date,
                                 rps.payment_schedule_date, rps.status, rps.name
                        ORDER by rps.created_date
            """;
    String countSizeListPaymentScheduleByLoanDetailInfoId = """
                SELECT count(*)
                FROM RepaymentPaymentSchedule rps
                WHERE rps.isDeleted = false
            """;


    List<PaymentSchedule> findByIsDeletedFalseAndIsPaidFalseAndDueDate(Timestamp dueDate);

    List<PaymentSchedule> findByIsDeletedFalseAndIsPaidFalseAndDueDateBefore(Timestamp date);

    Optional<PaymentSchedule> findByIdAndIsDeleted(String id, Boolean isDeleted);

    @Query(value = queryGetListPaymentScheduleByLoanDetailInfoId, countQuery = countSizeListPaymentScheduleByLoanDetailInfoId, nativeQuery = true)
    Page<RepaymentScheduleProjection> findPaymentScheduleByLoanDetailInfoId(String loanDetailInfoId, Pageable pageable);

    @Query(value = queryGetListPaymentScheduleByLoanDetailInfoId, nativeQuery = true)
    List<RepaymentScheduleProjection> findPaymentScheduleByLoanDetailInfoId(String loanDetailInfoId);

    List<PaymentSchedule> findAllByIsDeletedFalseAndLoanDetailInfo_IdAndDueDateAfterOrderByDueDateAsc(String loanDetailInfoId, Timestamp currentDate);

    Optional<PaymentSchedule> findFirstByIsDeletedFalseAndLoanDetailInfo_IdAndDueDateAfterOrderByDueDateAsc(String loanDetailInfoId, Timestamp currentDate);
}

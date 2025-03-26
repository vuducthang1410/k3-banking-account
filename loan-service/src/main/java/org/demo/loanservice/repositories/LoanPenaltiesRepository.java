package org.demo.loanservice.repositories;

import org.demo.loanservice.entities.LoanPenalties;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPenaltiesRepository extends JpaRepository<LoanPenalties,String> {
    List<LoanPenalties> findAllByIsPaidFalseAndIsDeletedFalseAndPaymentSchedule_IdIn(List<String>paymentScheduleId);
}

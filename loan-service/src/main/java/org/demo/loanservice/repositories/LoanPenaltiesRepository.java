package org.demo.loanservice.repositories;

import org.demo.loanservice.entities.LoanPenalties;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanPenaltiesRepository extends JpaRepository<LoanPenalties,String> {
}

package org.demo.loanservice.repositories;

import org.demo.loanservice.entities.RepaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentHistoryRepository extends JpaRepository<RepaymentHistory, String> {
}

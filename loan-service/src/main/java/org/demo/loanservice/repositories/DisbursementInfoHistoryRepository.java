package org.demo.loanservice.repositories;

import org.demo.loanservice.entities.DisbursementInfoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisbursementInfoHistoryRepository extends JpaRepository<DisbursementInfoHistory, String> {
}

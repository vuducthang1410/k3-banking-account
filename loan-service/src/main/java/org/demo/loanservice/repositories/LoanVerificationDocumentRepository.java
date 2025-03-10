package org.demo.loanservice.repositories;

import org.demo.loanservice.entities.IdClass.LoanVerificationDocumentId;
import org.demo.loanservice.entities.LoanVerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanVerificationDocumentRepository extends JpaRepository<LoanVerificationDocument, LoanVerificationDocumentId> {
}

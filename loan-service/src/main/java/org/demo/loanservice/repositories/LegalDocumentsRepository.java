package org.demo.loanservice.repositories;

import org.demo.loanservice.entities.LegalDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LegalDocumentsRepository extends JpaRepository<LegalDocuments, String> {
    String queryInsertFinancialInfoDocument= """
            INSERT INTO tbl_financial_info_document (financial_info_id, legal_document_id)
            VALUES (:financial_info_id, :legal_document_id)
            """;
    @Modifying
    @Query(value = queryInsertFinancialInfoDocument,nativeQuery = true)
    void insertFinancialInfoDocument(String financial_info_id, String legal_document_id);
}

package org.demo.loanservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.demo.loanservice.entities.IdClass.FinancialInfoDocumentId;
import org.hibernate.envers.Audited;

import java.io.Serializable;
@Entity
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_financial_info_document")
public class FinancialInfoDocument implements Serializable {
    @EmbeddedId
    private FinancialInfoDocumentId id;
    @ManyToOne
    @MapsId("financialInfoId")
    @JoinColumn(name = "financial_info_id")
    private FinancialInfo financialInfo;

    @OneToOne
    @MapsId("legalDocumentId")
    @JoinColumn(name = "legal_document_id")
    private LegalDocuments legalDocuments;
}

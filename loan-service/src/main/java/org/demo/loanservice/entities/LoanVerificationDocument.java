package org.demo.loanservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.demo.loanservice.entities.IdClass.LoanVerificationDocumentId;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_loan_verification_documents")
public class LoanVerificationDocument {
    @EmbeddedId
    private LoanVerificationDocumentId id = new LoanVerificationDocumentId();
    @MapsId("loanDetailInfoId")
    @ManyToOne
    @JoinColumn(name = "loan_detail_info_id")
    private LoanDetailInfo loanDetailInfo;

    @MapsId("legalDocumentsId")
    @ManyToOne
    @JoinColumn(name = "legal_document_id")
    private LegalDocuments legalDocuments;

//    @CreatedBy
//    @Column(updatable = false, nullable = false, length = 50, name = "CREATED_BY")
//    @Audited
//    private String createdBy;
//    @LastModifiedBy
//    @Column(length = 50,name = "LAST_MODIFIED_BY")
//    @Audited
//    private String lastModifiedBy;
    @CreatedDate
    @Column(updatable = false, nullable = false, length = 50, name = "CREATED_DATE")
    @Audited
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column( length = 50,name = "LAST_MODIFIED_DATE")
    @Audited
    private LocalDateTime lastModifiedDate;
    @Column( length = 1,name = "IS_DELETED")
    @Audited
    private Boolean isDeleted;
}

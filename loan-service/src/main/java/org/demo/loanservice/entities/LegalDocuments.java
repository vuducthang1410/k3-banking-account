package org.demo.loanservice.entities;


import jakarta.persistence.*;
import lombok.*;
import org.demo.loanservice.dto.enumDto.DocumentType;
import org.demo.loanservice.dto.enumDto.RequestStatus;
import org.hibernate.envers.Audited;

import java.sql.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_legal_documents")
@Audited
public class LegalDocuments extends BaseEntity {
    @Column(name = "customer_id")
    private String cifCode;
    private String description;
    private String urlDocument;
    @ManyToOne(fetch = FetchType.LAZY)
    private LegalDocuments documentGroupId;
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;
    @Temporal(TemporalType.DATE)
    private Date expirationDate;
    private String approvedBy;
}

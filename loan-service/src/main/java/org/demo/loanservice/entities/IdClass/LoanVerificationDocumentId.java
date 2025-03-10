package org.demo.loanservice.entities.IdClass;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanVerificationDocumentId implements Serializable {
    @Column(length = 50)
    private String loanDetailInfoId;
    @Column(length = 50)
    private String legalDocumentsId;
}

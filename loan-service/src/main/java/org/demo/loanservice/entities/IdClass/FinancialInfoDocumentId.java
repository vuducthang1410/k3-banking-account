package org.demo.loanservice.entities.IdClass;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
@Data
@Embeddable
public class FinancialInfoDocumentId implements Serializable {

    private String financialInfoId;
    private String legalDocumentId;
}
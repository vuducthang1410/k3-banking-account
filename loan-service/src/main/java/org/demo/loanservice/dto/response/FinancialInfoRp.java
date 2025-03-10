package org.demo.loanservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinancialInfoRp implements Serializable {
    private String customerId;
    private String customerName;
    private String numberPhone;
    private String identificationNumber;
    private String dateOfBirth;
    private String financialInfoId;
    private String income;
    private String unit;
    private String creditScore;
    private String incomeSource;
    private String incomeType;
    private String debtStatus;
    private Integer countLegalDocument;
    private Boolean isExpired;
    private String requestStatus;
    private List<LegalDocumentRp> legalDocumentRpList;
    private String expiredDate;
    private String note;
    private String amountLoanLimit;

}

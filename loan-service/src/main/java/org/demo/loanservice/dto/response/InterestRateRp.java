package org.demo.loanservice.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class InterestRateRp implements Serializable {
    private String id;
    private String interestRate;
    private String unit;
    private String isActive;
    private String minimumAmount;
    private String dateActive;
    private String minimumLoanTerm;
    private String createdDate;
}

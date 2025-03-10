package org.demo.loanservice.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoanTermRp implements Serializable {
    private String loanTermId;
    private Integer term;
    private String productId;
    private String unit;
}

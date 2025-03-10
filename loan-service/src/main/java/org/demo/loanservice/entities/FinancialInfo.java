package org.demo.loanservice.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.demo.loanservice.dto.enumDto.ApplicableObjects;
import org.demo.loanservice.dto.enumDto.RequestStatus;
import org.demo.loanservice.dto.enumDto.Unit;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Set;

@Entity
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_financial_info")
public class FinancialInfo extends BaseEntity{
    @Column(name = "customer_id")
    private String  customerId;
    private String cifCode;
    private String customerNumber;
    private String income;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    @Schema(description = "The user's credit score", example = "750")
    private Integer creditScore;
    @Schema(description = "Source of income", example = "Salary")
    private String incomeSource;
    @Schema(description = "Type of income, e.g., regular, irregular", example = "Regular")
    private String incomeType;
    @Enumerated(EnumType.STRING)
    private ApplicableObjects applicableObjects;
    private Timestamp lastUpdatedCreditReview;
    private String debtStatus;
    private Date expiredDate;
    private Boolean isExpired;
    private BigDecimal loanAmountMax;
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    private String note;
    @OneToMany(mappedBy = "financialInfo")
    private Set<FinancialInfoDocument> financialInfoDocumentSet;
}

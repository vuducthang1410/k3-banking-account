package org.demo.loanservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.demo.loanservice.dto.enumDto.FormDeftRepaymentEnum;
import org.demo.loanservice.dto.enumDto.LoanStatus;
import org.demo.loanservice.dto.enumDto.RequestStatus;
import org.demo.loanservice.dto.enumDto.Unit;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_loan_detail_info")
@Audited
public class LoanDetailInfo extends BaseEntity {
    @Column(name = "form_deft_repayment")
    @Enumerated(EnumType.STRING)
    private FormDeftRepaymentEnum formDeftRepayment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id")
    private LoanProduct loanProductId;
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;
    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus;
    private BigDecimal loanAmount;
    private Integer loanTerm;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    private Double interestRate;
    private String note;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_info_id")
    private FinancialInfo financialInfo;

    @OneToOne(mappedBy = "loanDetailInfo")
    private DisbursementInfoHistory disbursementInfoHistory;
}

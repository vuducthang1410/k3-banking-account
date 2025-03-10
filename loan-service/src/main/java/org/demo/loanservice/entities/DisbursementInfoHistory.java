package org.demo.loanservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_disbursement_info_history")
@Audited
public class DisbursementInfoHistory extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "loan_detail_info_id")
    private LoanDetailInfo loanDetailInfo;
    // todo: Persist financial information
    private Integer creditScore;
    private String income;
    private String incomeSource;
    private String incomeType;
    private String debtStatus;
    private Timestamp lastUpdatedCreditReview;
    private BigDecimal amountDisbursement;
    private String loanAccountId;
    private Timestamp loanDate;
    private Timestamp douDate;
}

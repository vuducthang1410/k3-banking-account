package org.demo.loanservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.demo.loanservice.dto.enumDto.DeftRepaymentStatus;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_payment_schedule")
@Audited
public class PaymentSchedule extends BaseEntity{
    private Timestamp paymentInterestDate;
    private Timestamp paymentScheduleDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_loan_info_id",nullable = false)
    private LoanDetailInfo loanDetailInfo;
    private String name;
    private Timestamp dueDate;
    private BigDecimal amountRepayment;
    private Boolean isPaid;
    private Boolean isPaidInterest;
    @Enumerated(EnumType.STRING)
    private DeftRepaymentStatus status;
    private BigDecimal amountInterestRate;

    @OneToMany(mappedBy = "paymentSchedule")
    Set<LoanPenalties> loanPenaltiesSet;
    @OneToMany(mappedBy = "paymentSchedule")
    Set<RepaymentHistory> repaymentHistorySet;
}

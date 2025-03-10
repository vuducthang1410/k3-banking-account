package org.demo.loanservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_loan_penalties")
@Audited
public class LoanPenalties extends BaseEntity{
//    @ManyToOne
//    @JoinColumn(name = "loan_detail_info_id")
//    private LoanDetailInfo loanDetailInfo;
    @ManyToOne
    @JoinColumn(name = "payment_schedule_id")
    private PaymentSchedule paymentSchedule;
    private String finedReason;
    private BigDecimal finedAmount;
    private Date finedDate;
    private Date finedPaymentDate;
    private Boolean isPaid;
}

package org.demo.loanservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.demo.loanservice.dto.enumDto.PaymentTransactionType;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_repayment_history")
@Audited
public class RepaymentHistory extends BaseEntity{
    private String transactionId;
    @ManyToOne
    @JoinColumn(name = "payment_schedule_id")
    private PaymentSchedule paymentSchedule;
    private String note;
    private BigDecimal amountPayment;
    @Enumerated(EnumType.STRING)
    private PaymentTransactionType paymentType;
}

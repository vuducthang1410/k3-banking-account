package org.demo.loanservice.entities;


import jakarta.persistence.*;
import lombok.*;
import org.demo.loanservice.dto.enumDto.Unit;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_interest_rate")
@Audited
public class InterestRate extends BaseEntity {
    private Double interestRate;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    private Timestamp dateActive;
    private Boolean isActive;
    private BigDecimal minimumAmount;
    private Integer minimumLoanTerm;
    @ManyToOne
    @JoinColumn(name = "loan_product_id")
    private LoanProduct loanProduct;
}

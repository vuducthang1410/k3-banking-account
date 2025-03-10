package com.system.account_service.entities;

import com.system.account_service.entities.type.InterestGroup;
import com.system.account_service.entities.type.Unit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "interest_rates")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRates extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String interestRateId;

    @Column(nullable = false)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestGroup interestGroup;

    private Integer minimumTerm;
    private Integer maximumTerm;

    @Column(nullable = false)
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }

        if(unit == null) {
            unit = Unit.MONTH;
        }

        if(interestGroup == null) {
            interestGroup = InterestGroup.CREDIT;
        }

        if(interestGroup.equals(InterestGroup.SAVING)) {
            if(minimumTerm == null) {
                minimumTerm = 1;
            }

            if(maximumTerm == null) {
                maximumTerm = 24;
            }
        }
    }
}

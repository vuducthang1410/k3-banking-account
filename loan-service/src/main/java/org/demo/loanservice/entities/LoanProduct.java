package org.demo.loanservice.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.demo.loanservice.dto.enumDto.ApplicableObjects;
import org.demo.loanservice.dto.enumDto.LoanType;
import org.demo.loanservice.dto.enumDto.Unit;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_loan_product")
@Audited
@Schema(description =
        "Represents a loan product with details related to loan terms," +
                " interest rates, collateral types, and other relevant features.")
public class LoanProduct extends BaseEntity {

    @Schema(description = "The form of the loan, e.g., consumer loan," +
            " mortgage loan, unsecured loan, etc.")
    @Enumerated(EnumType.STRING)
    private LoanType formLoan;

    @Schema(description = "The loan limit, indicating the maximum amount " +
            "a customer can borrow for this loan product, e.g., 100 million VND.")
    private BigDecimal loanLimit;

    @Schema(description = "The target borrower, describing the type of borrower " +
            "the loan product is intended for, e.g., individuals, households, businesses.")
    private String nameProduct;

    @Schema(description = "A detailed description of the loan product, including terms and" +
            " features of the loan.")
    private byte[] description;

    @Schema(description = "The interest rate ID associated with this loan product," +
            " linking to the interest rate table.")
    @OneToMany(mappedBy = "loanProduct", fetch = FetchType.LAZY)
    private Set<InterestRate> interestRateSet = new HashSet<>();

    @Schema(description = "Utility services associated with the loan product, " +
            "which may include insurance, financial planning services, etc.")
    private byte[] utilities;

    @Schema(description = "The URL to an image representing the loan product," +
            " such as a logo or promotional image.")
    private String productUrlImage;

    @Schema(description = "Loan conditions that borrowers must meet to qualify for" +
            " the loan, such as credit score, income, etc.")
    private byte[] loanCondition;

    @Enumerated(EnumType.STRING)
    private ApplicableObjects applicableObjects;

    private Integer termLimit;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    private Boolean isActive;
}


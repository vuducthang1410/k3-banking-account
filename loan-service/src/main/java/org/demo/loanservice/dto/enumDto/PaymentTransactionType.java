package org.demo.loanservice.dto.enumDto;

/**
 * Enum representing different types of payment transactions in loan history.
 */
public enum PaymentTransactionType {

    /** Payment of the principal amount of the loan */
    PRINCIPAL_PAYMENT,

    /** Payment of the interest amount of the loan */
    INTEREST_PAYMENT,

    /** Penalty fee applied for early repayment */
    EARLY_REPAYMENT_PENALTY,

    /** Penalty fee applied for overdue payment */
    OVERDUE_PAYMENT_PENALTY
}


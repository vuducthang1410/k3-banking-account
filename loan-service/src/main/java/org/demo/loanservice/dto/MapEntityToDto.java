package org.demo.loanservice.dto;

import org.demo.loanservice.common.DateUtil;
import org.demo.loanservice.common.Util;
import org.demo.loanservice.dto.projection.RepaymentScheduleProjection;
import org.demo.loanservice.dto.response.InterestRateRp;
import org.demo.loanservice.dto.response.PaymentScheduleRp;
import org.demo.loanservice.entities.InterestRate;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
public class MapEntityToDto {
    public static InterestRateRp convertToInterestRateRp(InterestRate interestRate) {
        InterestRateRp interestRateRp = new InterestRateRp();
        interestRateRp.setId(interestRate.getId());
        interestRateRp.setInterestRate(interestRate.getInterestRate().toString());
        interestRateRp.setUnit(interestRate.getUnit().name());
        interestRateRp.setIsActive(interestRate.getIsActive().toString());
        interestRateRp.setMinimumAmount(Util.formatToVND(interestRate.getMinimumAmount().stripTrailingZeros()));
        interestRateRp.setMinimumLoanTerm(interestRate.getMinimumLoanTerm().toString());
        interestRateRp.setDateActive(DateUtil.format(DateUtil.DD_MM_YYY_HH_MM_SLASH, new Date(interestRate.getDateActive().getTime())));
        interestRateRp.setCreatedDate(DateUtil.format(DateUtil.DD_MM_YYY_HH_MM_SLASH, Date.from(interestRate.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant())));
        return interestRateRp;
    }
    public static PaymentScheduleRp mapToPaymentScheduleRp(RepaymentScheduleProjection repaymentScheduleProjection) {
        PaymentScheduleRp paymentScheduleRp = new PaymentScheduleRp();
        paymentScheduleRp.setPaymentScheduleId(repaymentScheduleProjection.getId());
        paymentScheduleRp.setNameSchedule(repaymentScheduleProjection.getName());
        paymentScheduleRp.setStatus(repaymentScheduleProjection.getStatus());
        paymentScheduleRp.setDueDate(DateUtil.format(DateUtil.DD_MM_YYYY_SLASH, new Date(repaymentScheduleProjection.getDueDate().getTime())));

        BigDecimal amountRemaining = repaymentScheduleProjection.getAmountFinedRemaining()
                .add(repaymentScheduleProjection.getPaymentInterestRate() == null ? repaymentScheduleProjection.getAmountInterest() : BigDecimal.ZERO)
                .add(repaymentScheduleProjection.getPaymentScheduleDate() == null ? repaymentScheduleProjection.getAmountRepayment() : BigDecimal.ZERO);
        paymentScheduleRp.setAmountRemaining(amountRemaining.stripTrailingZeros().toPlainString());
        paymentScheduleRp.setIsPaid(amountRemaining.compareTo(BigDecimal.ZERO) == 0);
        return paymentScheduleRp;
    }
}

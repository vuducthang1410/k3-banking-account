package org.demo.loanservice.services.impl;

import com.system.common_library.dto.report.LoanReportRequest;
import com.system.common_library.dto.report.LoanReportResponse;
import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.response.loan.PaymentScheduleRp;
import com.system.common_library.enums.FormDeftRepaymentEnum;
import com.system.common_library.enums.LoanStatus;
import com.system.common_library.enums.Unit;
import com.system.common_library.exception.DubboException;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.LoanDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.common.DateUtil;
import org.demo.loanservice.common.MessageData;
import org.demo.loanservice.dto.enumDto.RequestStatus;
import org.demo.loanservice.dto.projection.LoanDetailReportProjection;
import org.demo.loanservice.dto.projection.RepaymentScheduleProjection;
import org.demo.loanservice.entities.LoanDetailInfo;
import org.demo.loanservice.repositories.LoanDetailInfoRepository;
import org.demo.loanservice.services.IPaymentScheduleService;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@DubboService
@RequiredArgsConstructor
public class LoanDubboServiceImpl implements LoanDubboService {
    private final LoanDetailInfoRepository loanDetailInfoRepository;
    private final IPaymentScheduleService paymentScheduleService;
    @DubboReference
    private CustomerDubboService customerDubboService;
    @DubboReference
    private AccountDubboService accountDubboService;

    private final Logger log = LogManager.getLogger(LoanDubboServiceImpl.class);

    @Override
    public void getLoanDetail(String account) {

    }

    @Override
    public void getLoanList(String cifCode) {

    }

    @Override
    public LoanReportResponse getLoanReport(String loanId) throws DubboException {
        String transactionId = UUID.randomUUID().toString();
        log.error("transaction Id - {}", transactionId);
        LoanDetailInfo loanDetailInfo = loanDetailInfoRepository.findByIdAndIsDeleted(loanId, false)
                .orElseThrow(() -> {
                    log.info(MessageData.MESSAGE_LOG, MessageData.LOAN_DETAIL_INFO_NOT_FOUND.getMessageLog(), loanId, UUID.randomUUID().toString());
                    return new DubboException(MessageData.LOAN_DETAIL_INFO_NOT_FOUND.getMessageLog());
                });
        try {
            AccountInfoDTO loanAccountInfo = accountDubboService.getLoanAccountDTO(loanDetailInfo.getDisbursementInfoHistory().getLoanAccountId());
            return mapObjectToLoanReportResponse(loanDetailInfo, loanAccountInfo, transactionId);
        } catch (DubboException de) {
            log.error("Execute error while get customer detail from customer dubbo service - ROOT CAUSE:: {}", de.getMessage(), de);
            throw de;
        } catch (Exception e) {
            log.error("Execute error while get customer detail from customer dubbo service -  ROOT CAUSE:: {}", e.getMessage(), e);
            throw new DubboException(e.getMessage());
        }
    }

    @Override
    public List<LoanReportResponse> getListLoanByField(LoanReportRequest request) throws DubboException {
        List<LoanDetailReportProjection> loanDetailReportProjectionList = loanDetailInfoRepository.findLoanDetailsToReport(
                request.getLoanId(),
                request.getCustomerId(),
                request.getMinLoanAmount(),
                request.getMaxLoanAmount(),
                request.getLoanType().name(),
                request.getLoanStatus().name(),
                request.getStartDate(),
                request.getEndDate());
        return loanDetailReportProjectionList.stream().map(this::mapToLoanReportResponse).toList();
    }

    @Override
    public List<LoanReportResponse> getReportLoansByList(List<String> loanId) throws DubboException {
        List<LoanDetailInfo> loanDetailInfoList = loanDetailInfoRepository.findAllByIsDeletedFalseAndRequestStatus(RequestStatus.APPROVED);
        return loanDetailInfoList.stream().map(this::mapToLoanReportResponse).toList();
    }

    private @NonNull LoanReportResponse mapObjectToLoanReportResponse(
            LoanDetailInfo loanDetailInfo,
            AccountInfoDTO loanAccountInfo,
            String transactionId) {
        LoanReportResponse loanReportResponse = new LoanReportResponse();
        loanReportResponse.setLoanId(loanDetailInfo.getId());
        loanReportResponse.setCustomerId(loanDetailInfo.getFinancialInfo().getCustomerId());
        loanReportResponse.setLoanAmount(loanDetailInfo.getLoanAmount().doubleValue());
        loanReportResponse.setLoanType(FormDeftRepaymentEnum.valueOf(loanDetailInfo.getFormDeftRepayment().name()));
        loanReportResponse.setRemainingBalance(loanAccountInfo.getCurrentAccountBalance().doubleValue());
        if (loanDetailInfo.getRequestStatus().equals(RequestStatus.APPROVED)) {
            List<PaymentScheduleRp> paymentScheduleRpList = paymentScheduleService
                    .getListPaymentScheduleByLoanDetailInfo(loanDetailInfo.getId(), transactionId)
                    .stream()
                    .map(this::mapToPaymentScheduleRp)
                    .toList();
            loanReportResponse.setPaymentScheduleList(paymentScheduleRpList);
        } else {
            loanReportResponse.setPaymentScheduleList(List.of());
        }
        loanReportResponse.setInterestRate(loanDetailInfo.getInterestRate());
        loanReportResponse.setAccountLoanNumber(loanAccountInfo.getAccountNumber());
        return loanReportResponse;
    }

    private @NonNull PaymentScheduleRp mapToPaymentScheduleRp(RepaymentScheduleProjection repaymentScheduleProjection) {
        PaymentScheduleRp paymentScheduleRp = new PaymentScheduleRp();
        paymentScheduleRp.setPaymentScheduleId(repaymentScheduleProjection.getId());
        paymentScheduleRp.setNameSchedule(repaymentScheduleProjection.getName());
        paymentScheduleRp.setStatus(repaymentScheduleProjection.getStatus());
        paymentScheduleRp.setDueDate(DateUtil.format(DateUtil.DD_MM_YYYY_SLASH, new Date(repaymentScheduleProjection.getDueDate().getTime())));
        BigDecimal amountRemaining = repaymentScheduleProjection.getAmountFinedRemaining()
                .add(repaymentScheduleProjection.getPaymentInterestRate() == null ? repaymentScheduleProjection.getAmountInterest() : BigDecimal.ZERO)
                .add(repaymentScheduleProjection.getPaymentScheduleDate() == null ? repaymentScheduleProjection.getAmountRepayment() : BigDecimal.ZERO);
        paymentScheduleRp.setAmountRemaining(amountRemaining.stripTrailingZeros().toPlainString());
        return paymentScheduleRp;
    }

    private LoanReportResponse mapToLoanReportResponse(LoanDetailInfo loanDetailInfo) {
        LoanReportResponse loanReportResponse = new LoanReportResponse();
        loanReportResponse.setLoanId(loanDetailInfo.getId());
        loanReportResponse.setCustomerId(loanDetailInfo.getFinancialInfo().getCustomerId());
        loanReportResponse.setLoanAmount(loanDetailInfo.getLoanAmount().doubleValue());
        loanReportResponse.setLoanType(FormDeftRepaymentEnum.valueOf(loanDetailInfo.getFormDeftRepayment().name()));
        loanReportResponse.setLoanStatus(LoanStatus.valueOf(loanDetailInfo.getLoanStatus().name()));
        loanReportResponse.setInterestRate(loanDetailInfo.getInterestRate());
        loanReportResponse.setUnit(Unit.valueOf(loanDetailInfo.getUnit().name()));
        AccountInfoDTO accountInfoDTO = accountDubboService.getLoanAccountDTO(loanDetailInfo.getDisbursementInfoHistory().getLoanAccountId());
        loanReportResponse.setAccountLoanNumber(accountInfoDTO.getAccountNumber());
        BigDecimal amountRemaining = loanDetailInfoRepository.getAmountRemainingByLoanDetailInfoId(loanDetailInfo.getId());
        loanReportResponse.setRemainingBalance(amountRemaining.doubleValue());
        List<PaymentScheduleRp> paymentScheduleRpList = paymentScheduleService.getListPaymentScheduleByLoanDetailInfo(loanDetailInfo.getId(), UUID.randomUUID().toString())
                .stream().map(this::mapToPaymentScheduleRp).toList();
        loanReportResponse.setPaymentScheduleList(paymentScheduleRpList);
        return loanReportResponse;
    }
    private LoanReportResponse mapToLoanReportResponse(LoanDetailReportProjection loanDetailInfo) {
        LoanReportResponse loanReportResponse = new LoanReportResponse();
        loanReportResponse.setLoanId(loanDetailInfo.getLoanId());
        loanReportResponse.setCustomerId(loanDetailInfo.getCustomerId());
        loanReportResponse.setLoanAmount(loanDetailInfo.getLoanAmount());
        loanReportResponse.setLoanType(FormDeftRepaymentEnum.valueOf(loanDetailInfo.getLoanType()));
        loanReportResponse.setLoanStatus(LoanStatus.valueOf(loanDetailInfo.getLoanStatus()));
        loanReportResponse.setInterestRate(loanDetailInfo.getInterestRate());
        loanReportResponse.setUnit(Unit.valueOf(loanDetailInfo.getUnit()));
        AccountInfoDTO accountInfoDTO = accountDubboService.getLoanAccountDTO(loanDetailInfo.getLoanAccountId());
        loanReportResponse.setAccountLoanNumber(accountInfoDTO.getAccountNumber());
        BigDecimal amountRemaining = loanDetailInfoRepository.getAmountRemainingByLoanDetailInfoId(loanDetailInfo.getLoanId());
        loanReportResponse.setRemainingBalance(amountRemaining.doubleValue());
        List<PaymentScheduleRp> paymentScheduleRpList = paymentScheduleService.getListPaymentScheduleByLoanDetailInfo(loanDetailInfo.getLoanId(), UUID.randomUUID().toString())
                .stream().map(this::mapToPaymentScheduleRp).toList();
        loanReportResponse.setPaymentScheduleList(paymentScheduleRpList);
        return loanReportResponse;
    }
}
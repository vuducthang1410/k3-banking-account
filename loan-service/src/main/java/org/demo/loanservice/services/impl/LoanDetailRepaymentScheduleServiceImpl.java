package org.demo.loanservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.common.MessageData;
import org.demo.loanservice.controllers.exception.DataNotValidException;
import org.demo.loanservice.dto.enumDto.LoanStatus;
import org.demo.loanservice.entities.LoanDetailInfo;
import org.demo.loanservice.repositories.LoanDetailInfoRepository;
import org.demo.loanservice.services.ILoanDetailRepaymentScheduleService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanDetailRepaymentScheduleServiceImpl implements ILoanDetailRepaymentScheduleService {
    private final LoanDetailInfoRepository loanDetailInfoRepository;
    private final Logger log = LogManager.getLogger(LoanDetailRepaymentScheduleServiceImpl.class);

    @Override
    public LoanDetailInfo getLoanDetailInfoById(String loanDetailInfoId, String transactionId) {
        return loanDetailInfoRepository
                .findByIdAndIsDeleted(loanDetailInfoId, false)
                .orElseThrow(() -> {
                    log.info(MessageData.MESSAGE_LOG_NOT_FOUND_DATA, transactionId, MessageData.LOAN_DETAIL_INFO_NOT_FOUND.getMessageLog(), loanDetailInfoId);
                    return new DataNotValidException(MessageData.LOAN_DETAIL_INFO_NOT_FOUND);
                });
    }

    @Override
    public void updateLoanStatus(String loanDetailInfoId, String transactionId) {
        LoanDetailInfo loanDetailInfo=getLoanDetailInfoById(loanDetailInfoId, transactionId);
        loanDetailInfo.setLoanStatus(LoanStatus.PAID_OFF);
        loanDetailInfoRepository.save(loanDetailInfo);
    }
}

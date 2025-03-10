package org.demo.loanservice.services;

import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.dto.request.InterestRateRq;
import org.demo.loanservice.entities.InterestRate;

import java.math.BigDecimal;
import java.util.List;

public interface IInterestRateService extends IBaseService<InterestRateRq>{
    InterestRate getInterestRateById(String id, String transactionId);

    InterestRate getInterestRateByLoanAmount(BigDecimal loanAmount, String transactionId);

    List<InterestRate> interestRateList(List<String> listLoanProduct);

    List<InterestRate> interestRateListByActiveTrue(List<String> listLoanProduct);

    DataResponseWrapper<Object> getAllInterestRateByLoanProductId(Integer pageNumber, Integer pageSize, String loanProductId, String transactionId);
}

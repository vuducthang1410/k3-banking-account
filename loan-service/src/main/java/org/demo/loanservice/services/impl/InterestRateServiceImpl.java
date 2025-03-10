package org.demo.loanservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.demo.loanservice.common.*;
import org.demo.loanservice.controllers.exception.DataNotFoundException;
import org.demo.loanservice.dto.MapEntityToDto;
import org.demo.loanservice.dto.enumDto.Unit;
import org.demo.loanservice.dto.request.InterestRateRq;
import org.demo.loanservice.dto.response.InterestRateRp;
import org.demo.loanservice.entities.InterestRate;
import org.demo.loanservice.entities.LoanProduct;
import org.demo.loanservice.repositories.InterestRateRepository;
import org.demo.loanservice.repositories.LoanProductRepository;
import org.demo.loanservice.services.IInterestRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class InterestRateServiceImpl implements IInterestRateService {
    private final InterestRateRepository interestRateRepository;
    private final LoanProductRepository loanProductRepository;
    private final Util util;
    private final Logger log = LoggerFactory.getLogger(InterestRateServiceImpl.class);

    @Override
    public DataResponseWrapper<Object> save(InterestRateRq interestRateRq, String transactionId) {
        Optional<LoanProduct> loanProductOptional = loanProductRepository.findByIdAndIsDeleted(interestRateRq.getLoanProductId(), Boolean.FALSE);
        if (loanProductOptional.isEmpty()) {
            log.info(MessageData.MESSAGE_LOG_NOT_FOUND_DATA, transactionId, MessageData.LOAN_PRODUCT_NOT_FOUNT.getMessageLog(), interestRateRq.getLoanProductId());
            throw new DataNotFoundException(MessageData.LOAN_PRODUCT_NOT_FOUNT);
        }
        LoanProduct loanProduct = loanProductOptional.get();
        InterestRate interestRate = new InterestRate();
        interestRate.setInterestRate(interestRateRq.getInterestRate());
        interestRate.setUnit(Unit.valueOf(interestRateRq.getUnit()));
        interestRate.setMinimumAmount(interestRateRq.getMinimumAmount());
        interestRate.setMinimumLoanTerm(interestRateRq.getMinimumLoanTerm());
        interestRate.setIsActive(false);
        interestRate.setIsDeleted(false);
        interestRate.setDateActive(DateUtil.getCurrentTimeUTC7());
        interestRate.setLoanProduct(loanProduct);
        interestRateRepository.save(interestRate);
        return DataResponseWrapper.builder()
                .data(Map.of("InterestRateId", interestRate.getId()))
                .message(util.getMessageFromMessageSource(MessageData.CREATED_SUCCESSFUL.getKeyMessage()))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    @Cacheable(value = "interest_rate", key = "#id", unless = "#result == null")
    public DataResponseWrapper<Object> getById(String id, String transactionId) {
        Optional<InterestRate> optionalInterestRate = interestRateRepository.findInterestRateByIdAndIsDeleted(id, false);
        if (optionalInterestRate.isEmpty()) {
            log.info(MessageData.MESSAGE_LOG_NOT_FOUND_DATA, transactionId, MessageData.INTEREST_RATE_NOT_FOUND.getMessageLog(), id);
            throw new DataNotFoundException(MessageData.INTEREST_RATE_NOT_FOUND);
        }
        return DataResponseWrapper.builder()
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                .data(MapEntityToDto.convertToInterestRateRp(optionalInterestRate.get()))
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getAll(Integer pageNumber, Integer pageSize, String transactionId) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate"));
        Page<InterestRate> interestRatePage = interestRateRepository.findAllByIsDeleted(false, pageable);
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("totalRecord", interestRatePage.getTotalElements());
        List<InterestRateRp> interestRateRpList = interestRatePage.stream().map(
                MapEntityToDto::convertToInterestRateRp
        ).toList();
        dataResponse.put("interestRateList", interestRateRpList);
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    @CacheEvict(value = "interest_rate", key = "#id")
    public DataResponseWrapper<Object> active(String id, String transactionId) {
        InterestRate interestRate = getInterestRateById(id, transactionId);
        interestRate.setIsActive(!interestRate.getIsActive());
        interestRate.setDateActive(DateUtil.getCurrentTimeUTC7());
        interestRateRepository.save(interestRate);
        return DataResponseWrapper.builder()
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message("Active interest rate successfully")
                .data(MapEntityToDto.convertToInterestRateRp(interestRate))
                .build();
    }

    @Override
    @CacheEvict(value = "interest_rate", key = "#id")
    public DataResponseWrapper<Object> update(String id, InterestRateRq interestRateRq, String transactionId) {
        InterestRate interestRate = getInterestRateById(id, transactionId);
        interestRate.setUnit(Unit.valueOf(interestRateRq.getUnit()));
        interestRate.setInterestRate(interestRateRq.getInterestRate());
        interestRate.setMinimumAmount(interestRateRq.getMinimumAmount());
        interestRate.setMinimumLoanTerm(interestRateRq.getMinimumLoanTerm());
        interestRate.setIsActive(false);
        interestRateRepository.save(interestRate);
        return DataResponseWrapper.builder()
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message("Active interest rate successfully")
                .data(MapEntityToDto.convertToInterestRateRp(interestRate))
                .build();
    }

    @Override
    @CacheEvict(value = "interest_rate", key = "#id")
    public DataResponseWrapper<Object> delete(String id, String transactionId) {
        InterestRate interestRate = getInterestRateById(id, transactionId);
        interestRate.setIsDeleted(true);
        interestRateRepository.save(interestRate);
        return DataResponseWrapper.builder()
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message("delete successfully")
                .data(interestRate.getId())
                .build();
    }

    @Override
    public InterestRate getInterestRateById(String id, String transactionId) {
        Optional<InterestRate> optionalInterestRate = interestRateRepository.findInterestRateByIdAndIsDeleted(id, false);
        if (optionalInterestRate.isEmpty()) {
            log.info(MessageData.MESSAGE_LOG_NOT_FOUND_DATA, transactionId, MessageData.INTEREST_RATE_NOT_FOUND.getMessageLog(), id);
            throw new DataNotFoundException(MessageData.INTEREST_RATE_NOT_FOUND);
        }
        return optionalInterestRate.get();
    }

    @Override
    public InterestRate getInterestRateByLoanAmount(BigDecimal loanAmount, String transactionId) {
        log.debug("transactionId: {} - Loan amount : {}", transactionId, loanAmount.toPlainString());
        Optional<InterestRate> optionalInterestRate = interestRateRepository.findFirstByMinimumAmountLessThanEqualAndIsDeletedAndIsActiveTrueOrderByMinimumAmount(loanAmount, false);
        if (optionalInterestRate.isEmpty()) {
            log.info(MessageData.MESSAGE_LOG, transactionId, MessageData.INTEREST_RATE_VALID_NOT_FOUND.getMessageLog(), "Greater than ".concat(loanAmount.toPlainString()));
            throw new DataNotFoundException(MessageData.INTEREST_RATE_VALID_NOT_FOUND);
        }
        return optionalInterestRate.get();
    }

    @Override
    public List<InterestRate> interestRateList(List<String> listLoanProduct) {
        return interestRateRepository.findValidInterestRates(listLoanProduct);
    }

    @Override
    public List<InterestRate> interestRateListByActiveTrue(List<String> listLoanProduct) {
        return interestRateRepository.findValidInterestRatesByIsActiveTrue(listLoanProduct);
    }

    @Override
    public DataResponseWrapper<Object> getAllInterestRateByLoanProductId(Integer pageNumber, Integer pageSize, String loanProductId, String transactionId) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate"));
        Page<InterestRate> interestRatePage = interestRateRepository.findAllByIsDeletedAndLoanProductId(false, loanProductId, pageable);
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("totalRecord", interestRatePage.getTotalElements());
        List<InterestRateRp> interestRateRpList = interestRatePage.stream().map(
                MapEntityToDto::convertToInterestRateRp
        ).toList();
        dataResponse.put("interestRateList", interestRateRpList);
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }
}

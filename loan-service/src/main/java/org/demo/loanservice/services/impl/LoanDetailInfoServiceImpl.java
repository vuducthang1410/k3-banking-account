package org.demo.loanservice.services.impl;

import com.system.common_library.dto.account.CreateDubboLoanDTO;
import com.system.common_library.dto.notifcation.rabbitMQ.LoanAccountNoti;
import com.system.common_library.dto.notifcation.rabbitMQ.LoanDisbursementSuccessNoti;
import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.response.account.LoanAccountInfoDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanDisbursementTransactionDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanPaymentTransactionDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanTransactionDTO;
import com.system.common_library.dto.transaction.loan.TransactionLoanResultDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.ObjectStatus;
import com.system.common_library.exception.DubboException;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.TransactionDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.common.*;
import org.demo.loanservice.controllers.exception.DataNotFoundException;
import org.demo.loanservice.controllers.exception.DataNotValidException;
import org.demo.loanservice.controllers.exception.DataNotValidWithConditionException;
import org.demo.loanservice.controllers.exception.ServerErrorException;
import org.demo.loanservice.dto.MapEntityToDto;
import org.demo.loanservice.dto.enumDto.*;
import org.demo.loanservice.dto.projection.LoanAmountInfoProjection;
import org.demo.loanservice.dto.projection.LoanDetailActiveHistoryProjection;
import org.demo.loanservice.dto.projection.LoanInfoDetailProjection;
import org.demo.loanservice.dto.projection.RepaymentScheduleProjection;
import org.demo.loanservice.dto.request.IndividualCustomerInfoRq;
import org.demo.loanservice.dto.request.LoanInfoApprovalRq;
import org.demo.loanservice.dto.response.CustomerLoanDetailInfoRp;
import org.demo.loanservice.dto.response.LoanDetailInfoActiveRp;
import org.demo.loanservice.dto.response.LoanDetailInfoRp;
import org.demo.loanservice.dto.response.PaymentScheduleRp;
import org.demo.loanservice.entities.*;
import org.demo.loanservice.repositories.DisbursementInfoHistoryRepository;
import org.demo.loanservice.repositories.LoanDetailInfoRepository;
import org.demo.loanservice.repositories.LoanVerificationDocumentRepository;
import org.demo.loanservice.services.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanDetailInfoServiceImpl implements ILoanDetailInfoService {
    private final LoanDetailInfoRepository loanDetailInfoRepository;
    private final LoanVerificationDocumentRepository loanVerificationDocumentRepository;
    private final IInterestRateService interestRateService;
    private final ILoanProductService loanProductService;
    private final IFinancialInfoService financialInfoService;
    private final IPaymentScheduleService paymentScheduleService;
    private final ILoanDetailRepaymentScheduleService loanDetailRepaymentScheduleService;
    private final DisbursementInfoHistoryRepository disbursementInfoHistoryRepository;
    @DubboReference
    private CustomerDubboService customerDubboService;
    @DubboReference
    private TransactionDubboService transactionDubboService;
    @DubboReference
    private AccountDubboService accountDubboService;
    private final INotificationService notificationService;
    private final Util util;
    private final Logger log = LogManager.getLogger(LoanDetailInfoServiceImpl.class);

    @Override
    public DataResponseWrapper<Object> registerIndividualCustomerLoan(IndividualCustomerInfoRq individualCustomerInfoRq, String transactionId) {
        // Retrieve the CIF code of the authenticated customer
        CustomerDetailDTO customerInfo = customerDubboService.getCustomerByCifCode(individualCustomerInfoRq.getCifCode());
        // Retrieve financial information for the customer
        FinancialInfo financialInfo = financialInfoService.getFinancialInfoByCustomerId(customerInfo.getCustomerId(), transactionId);

        // Validate if the financial information is approved
        if (!financialInfo.getRequestStatus().equals(RequestStatus.APPROVED)) {
            log.info(MessageData.MESSAGE_LOG, MessageData.FINANCIAL_INFO_NOT_APPROVE.getMessageLog(), transactionId);
            throw new DataNotValidException(MessageData.FINANCIAL_INFO_NOT_APPROVE);
        }

        // Retrieve the loan product details
        LoanProduct loanProduct = loanProductService.getLoanProductById(individualCustomerInfoRq.getLoanProductId(), transactionId);

        // Validate if the requested loan amount does not exceed the product's loan limit
        if (loanProduct.getLoanLimit().compareTo(individualCustomerInfoRq.getLoanAmount()) < 0) {
            log.info(MessageData.MESSAGE_LOG, transactionId, String.format(MessageData.LOAN_AMOUNT_LARGER_LOAN_LIMIT.getMessageLog(), loanProduct.getLoanLimit().toPlainString()));
            throw new DataNotValidException(MessageData.LOAN_AMOUNT_LARGER_LOAN_LIMIT);
        }

        // Validate if the requested loan term does not exceed the product's term limit
        if (loanProduct.getTermLimit().compareTo(individualCustomerInfoRq.getLoanTerm()) < 0) {
            log.info(MessageData.MESSAGE_LOG, transactionId, String.format(MessageData.LOAN_TERM_LARGER_THAN_LIMIT.getMessageLog(), loanProduct.getTermLimit()));
            throw new DataNotValidException(MessageData.LOAN_TERM_LARGER_THAN_LIMIT);
        }

        Optional<LoanAmountInfoProjection> loanAmountInfoProjectionOptional = loanDetailInfoRepository.getMaxLoanLimitAndCurrentLoanAmount(customerInfo.getCustomerId());
        if (loanAmountInfoProjectionOptional.isEmpty()) {
            log.info(MessageData.MESSAGE_LOG_NOT_FOUND_DATA, transactionId, "Not found information for loan amount and loan amount limit of customer with ", customerInfo.getCustomerId());
            throw new DataNotValidException(MessageData.LOAN_TERM_LARGER_THAN_LIMIT);
        }
        LoanAmountInfoProjection loanAmountInfoProjection = loanAmountInfoProjectionOptional.get();
        BigDecimal expectedLoanAmount = individualCustomerInfoRq.getLoanAmount().add(loanAmountInfoProjection.getTotalLoanedAmount());
        if (expectedLoanAmount.compareTo(loanAmountInfoProjection.getLoanAmountMax()) > 0) {
            String messageLog = String.format(MessageData.LOAN_AMOUNT_LARGER_LOAN_REMAINING_LIMIT.getMessageLog(),
                    expectedLoanAmount.toPlainString(),
                    loanAmountInfoProjection.getLoanAmountMax().toPlainString());
            String amountLoanRemainingExpect = financialInfo.getLoanAmountMax().subtract(loanAmountInfoProjection.getTotalLoanedAmount()).stripTrailingZeros().toPlainString();
            log.info(MessageData.MESSAGE_LOG, transactionId, messageLog);

            throw new DataNotValidWithConditionException(MessageData.LOAN_AMOUNT_LARGER_LOAN_REMAINING_LIMIT, amountLoanRemainingExpect);
        }

        // Retrieve the applicable interest rate based on the loan amount
        InterestRate interestRate = interestRateService.getInterestRateByLoanAmount(individualCustomerInfoRq.getLoanAmount(), transactionId);
        log.debug("transactionId:{} - Loan amount : {} - loan term {} ",
                transactionId,
                individualCustomerInfoRq.getLoanAmount(),
                individualCustomerInfoRq.getLoanTerm());
        log.debug("transactionId:{} - interest rate: {} - minimum amount: {} - minimum term: {}",
                transactionId, interestRate.getInterestRate(),
                interestRate.getMinimumAmount(),
                interestRate.getMinimumLoanTerm());

        // Create a new loan detail entry
        LoanDetailInfo loanDetailInfo = new LoanDetailInfo();
        loanDetailInfo.setLoanAmount(individualCustomerInfoRq.getLoanAmount());
        loanDetailInfo.setLoanTerm(individualCustomerInfoRq.getLoanTerm());
        loanDetailInfo.setRequestStatus(RequestStatus.PENDING);
        loanDetailInfo.setLoanProductId(loanProduct);
        loanDetailInfo.setFormDeftRepayment(FormDeftRepaymentEnum.valueOf(individualCustomerInfoRq.getFormDeftRepayment()));
        loanDetailInfo.setLoanStatus(LoanStatus.PENDING);
        loanDetailInfo.setUnit(Unit.valueOf(individualCustomerInfoRq.getLoanUnit()));
        loanDetailInfo.setFinancialInfo(financialInfo);
        loanDetailInfo.setInterestRate(interestRate.getInterestRate());
        // Save loan details to the database
        loanDetailInfoRepository.save(loanDetailInfo);

        // Create and store loan verification documents based on the customer's financial information documents
        List<LoanVerificationDocument> loanVerificationDocumentList = new ArrayList<>();
        financialInfo.getFinancialInfoDocumentSet().forEach(financialInfoDocument -> {
            LoanVerificationDocument loanVerificationDocument = new LoanVerificationDocument();
            loanVerificationDocument.setLoanDetailInfo(loanDetailInfo);
            loanVerificationDocument.setLegalDocuments(financialInfoDocument.getLegalDocuments());
            loanVerificationDocumentList.add(loanVerificationDocument);
        });

        // Save all loan verification documents
        loanVerificationDocumentRepository.saveAll(loanVerificationDocumentList);
        //todo:Call notification service
        return DataResponseWrapper.builder()
                .data(loanDetailInfo.getId())
                .message("Loan registration successful")
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    @Transactional
    public DataResponseWrapper<Object> approveIndividualCustomerDisbursement(LoanInfoApprovalRq loanInfoApprovalRq, String transactionId) {
        LoanDetailInfo loanDetailInfo = loanDetailRepaymentScheduleService.getLoanDetailInfoById(loanInfoApprovalRq.getLoanDetailInfoId(), transactionId);
        if (!loanDetailInfo.getRequestStatus().equals(RequestStatus.PENDING)) {
            String messageLog = String.format(MessageData.REQUEST_STATUS_LOAN_NOT_PENDING.getMessageLog(), loanDetailInfo.getId(), loanDetailInfo.getRequestStatus().name());
            log.debug(MessageData.MESSAGE_LOG, transactionId, messageLog);
            throw new DataNotValidException(MessageData.REQUEST_STATUS_LOAN_NOT_PENDING);
        }
        log.debug("Request status of loan info approved:{}", loanInfoApprovalRq.getRequestStatus());
        if (loanInfoApprovalRq.getRequestStatus().equalsIgnoreCase(RequestStatus.REJECTED.name())) {
            loanDetailInfo.setRequestStatus(RequestStatus.REJECTED);
            loanDetailInfo.setNote(loanDetailInfo.getNote());
            loanDetailInfo.setLoanStatus(LoanStatus.REJECTED);
//            notificationService.sendNoti
            loanDetailInfoRepository.save(loanDetailInfo);
            return DataResponseWrapper.builder()
                    .data(loanDetailInfo.getId())
                    .message("")
                    .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                    .build();
        }
        try {
            // Retrieve customer banking account information
            AccountInfoDTO bankingAccountDTO = accountDubboService.getBankingAccount(loanDetailInfo.getFinancialInfo().getCifCode());

            //create loan account
            CreateDubboLoanDTO createLoanDTO = new CreateDubboLoanDTO();
            createLoanDTO.setCustomerId(loanDetailInfo.getFinancialInfo().getCustomerId());
            createLoanDTO.setCifCode(loanDetailInfo.getFinancialInfo().getCifCode());
            createLoanDTO.setBranchId("54a47e95-ff4f-480f-98ed-63cc9bc98ca8");
            LoanAccountInfoDTO loanAccountInfoDTO = accountDubboService.createLoanAccount(createLoanDTO);
            // Check for null separately
            if (loanAccountInfoDTO == null) {
                log.error(MessageData.MESSAGE_LOG, transactionId, "Loan account creation failed.");
                throw new DataNotValidException(MessageData.CREATED_LOAN_ACCOUNT_ERROR);
            }
            if (bankingAccountDTO == null || !StringUtils.hasText(bankingAccountDTO.getAccountNumber())) {
                log.error("transactionId : {} :: Execute error while get banking account. customer id: {}", transactionId, loanDetailInfo.getFinancialInfo().getCustomerId());
                throw new DataNotValidException(MessageData.BANKING_ACCOUNT_NOT_EXITS);
            }
            if (!bankingAccountDTO.getStatusAccount().equals(ObjectStatus.ACTIVE) || !loanAccountInfoDTO.getStatusLoanAccount().equals(ObjectStatus.ACTIVE)) {
                log.error("transactionId : {} :: Execute error while get banking account. customer id: {}", transactionId, loanDetailInfo.getFinancialInfo().getCustomerId());
                throw new DataNotValidException(MessageData.BANKING_ACCOUNT_NOT_ACTIVE);
            }

            //Disbursement for loan
            CreateLoanDisbursementTransactionDTO loanDisbursementTransactionDTO = getCreateLoanDisbursementTransactionDTO(loanAccountInfoDTO, loanDetailInfo, bankingAccountDTO);
            TransactionLoanResultDTO transactionResponse = transactionDubboService.createLoanAccountDisbursement(loanDisbursementTransactionDTO);
            if (transactionResponse == null) {
                log.error(MessageData.MESSAGE_LOG_DETAIL, transactionId, MessageData.DATA_RESPONSE_TRANSACTION_SERVICE_NOT_VALID.getMessageLog(), "Data response is null");
                throw new DataNotValidException(MessageData.SERVER_ERROR);
            }
            //handler response from transaction service
            loanDetailInfo.setRequestStatus(RequestStatus.valueOf(loanInfoApprovalRq.getRequestStatus()));
            loanDetailInfo.setNote(loanDetailInfo.getNote());
            loanDetailInfo.setLoanStatus(LoanStatus.ACTIVE);

            //create disbursement info history
            DisbursementInfoHistory disbursementInfoHistory = getDisbursementInfoHistory(loanDetailInfo, loanAccountInfoDTO, transactionResponse);
            disbursementInfoHistoryRepository.saveAndFlush(disbursementInfoHistory);
            loanDetailInfo.setDisbursementInfoHistory(disbursementInfoHistory);
            loanDetailInfoRepository.saveAndFlush(loanDetailInfo);
            //generate deft repayment schedule
            paymentScheduleService.createDeftRepaymentInfo(loanDetailInfo);

            //call notification service
            //loan approved success
            LoanAccountNoti loanAccountNoti = new LoanAccountNoti();
            loanAccountNoti.setAccountNumber(loanAccountInfoDTO.getLoanAccountNumber());
            loanAccountNoti.setLoanDueAmount(disbursementInfoHistory.getAmountDisbursement());
            loanAccountNoti.setCustomerCIF(loanDetailInfo.getFinancialInfo().getCifCode());
            loanAccountNoti.setLoanDueDate(DateUtil.convertTimeStampToLocalDate(disbursementInfoHistory.getLoanDate()));
            loanAccountNoti.setOpenDate(DateUtil.convertTimeStampToLocalDate(disbursementInfoHistory.getDouDate()));
//            notificationService.sendNotificationApprovedLoanSuccess(loanAccountNoti);
            //disbursement success
            LoanDisbursementSuccessNoti loanDisbursementSuccessNoti=new LoanDisbursementSuccessNoti();
            loanDisbursementSuccessNoti.setLoanAmount(disbursementInfoHistory.getAmountDisbursement());
            loanDisbursementSuccessNoti.setCustomerCIF(loanDetailInfo.getFinancialInfo().getCifCode());
            loanDisbursementSuccessNoti.setDisbursementDate(DateUtil.convertTimeStampToLocalDate(disbursementInfoHistory.getLoanDate()));
            loanDisbursementSuccessNoti.setBankAccount(bankingAccountDTO.getAccountNumber());
            notificationService.sendNotificationDisbursementSuccess(loanDisbursementSuccessNoti);
        } catch (DubboException e) {
            log.info(MessageData.MESSAGE_LOG, transactionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.info(MessageData.MESSAGE_LOG, transactionId, e.getMessage());
            //todo: callback transaction
            throw new ServerErrorException(transactionId, MessageData.APPROVE_INDIVIDUAL_CUSTOMER_DISBURSEMENT_ERROR.getCode());

        }
        return DataResponseWrapper.builder()
                .data(loanDetailInfo.getId())
                .message(util.getMessageFromMessageSource(MessageData.CREATED_SUCCESSFUL.getKeyMessage()))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    private static @NotNull CreateLoanDisbursementTransactionDTO getCreateLoanDisbursementTransactionDTO(LoanAccountInfoDTO loanAccountInfoDTO, LoanDetailInfo loanDetailInfo, AccountInfoDTO bankingAccountDTO) {
        CreateLoanDisbursementTransactionDTO loanDisbursementTransactionDTO = new CreateLoanDisbursementTransactionDTO();
        loanDisbursementTransactionDTO.setLoanAccount(loanAccountInfoDTO.getLoanAccountNumber());
        loanDisbursementTransactionDTO.setNote("");
        loanDisbursementTransactionDTO.setAmount(loanDetailInfo.getLoanAmount());
        loanDisbursementTransactionDTO.setCifCode(loanDetailInfo.getFinancialInfo().getCifCode());
        loanDisbursementTransactionDTO.setPaymentAccount(bankingAccountDTO.getAccountNumber());
        loanDisbursementTransactionDTO.setDescription("");
        return loanDisbursementTransactionDTO;
    }


    @Override
    public DataResponseWrapper<Object> getAllByLoanStatus(String requestStatus, Integer pageNumber, Integer pageSize, String transactionId) {
        log.debug("Loan status : {}", requestStatus);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate"));
        Page<LoanDetailInfo> loanDetailInfoPage = loanDetailInfoRepository.findAllByIsDeletedFalseAndRequestStatus(RequestStatus.valueOf(requestStatus), pageable);
        List<LoanDetailInfo> loanDetailInfoList = loanDetailInfoPage.getContent();
        log.debug("size of loan detail info list :{}", loanDetailInfoList.size());
        if (loanDetailInfoList.isEmpty()) {
            return DataResponseWrapper.builder()
                    .data(Collections.emptyList())
                    .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                    .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                    .build();
        }
        List<String> listCifCodeOfLoan = loanDetailInfoList.stream().map(e -> e.getFinancialInfo().getCifCode()).toList();
        try {
            //Get customer info by customer id from dubbo service
            List<CustomerDetailDTO> customerDetailDTOList = customerDubboService.getListCustomerByCifCode(listCifCodeOfLoan);
            log.debug("Size of customer detail dto list : {}", customerDetailDTOList.size());
            // Convert list to map
            Map<String, CustomerDetailDTO> customerDetailDTOMap = customerDetailDTOList.stream()
                    .collect(Collectors.toMap(
                            CustomerDetailDTO::getCustomerId,
                            Function.identity(),
                            (existing, replacement) -> existing // (void duplicate key issue)Keep the existing entry in case of duplicate
                    ));
            //convert to response to dto response
            List<CustomerLoanDetailInfoRp> customerLoanDetailInfoRpList = loanDetailInfoList
                    .stream()
                    .map(e -> {
                        CustomerDetailDTO customerDetailDTO = customerDetailDTOMap.get(e.getFinancialInfo().getCustomerId());
                        return mapObjectToCustomerLoanDetailInfoDto(e, customerDetailDTO);
                    })
                    .toList();
            Map<String, Object> dataResponse = new HashMap<>();
            dataResponse.put("totalRecord", loanDetailInfoPage.getTotalElements());
            dataResponse.put("dataResponse", customerLoanDetailInfoRpList);
            return DataResponseWrapper.builder()
                    .data(dataResponse)
                    .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                    .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                    .build();
        } catch (DubboException de) {
            log.error(MessageData.MESSAGE_LOG_DETAIL, transactionId, "Execute error while get all loan detail by loan status ", de.getMessage());
            throw de;
        } catch (Exception e) {
            log.error(MessageData.MESSAGE_LOG_DETAIL, transactionId, "Execute error while get all loan detail by loan status ", e.getMessage(), e);
            throw new ServerErrorException();
        }
    }

    @Override
    public DataResponseWrapper<Object> getAllByCifCode(Integer pageNumber, Integer pageSize, String transactionId, String requestStatus, String cifCode) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        List<FinancialInfo> financialInfoList = financialInfoService.getListFinancialInfoByCifCode(cifCode, transactionId);
        log.debug("Size of financial info list : {}", financialInfoList.size());
        if (financialInfoList.isEmpty()) {
            log.info("not found financial info so can not find loan detail info");
            throw new DataNotFoundException(MessageData.FINANCIAL_INFO_NOT_FOUND);
        }
        List<String> financialInfoIsList = financialInfoList.stream().map(FinancialInfo::getId).toList();
        Page<LoanInfoDetailProjection> loanDetailInfoPage = loanDetailInfoRepository.findAllLoanInfoByFinancialInfoList(financialInfoIsList, requestStatus, pageable);
        List<LoanInfoDetailProjection> loanDetailInfoList = loanDetailInfoPage.getContent();
        log.debug("Total element expect : {}", loanDetailInfoPage.getTotalElements());
        log.debug("Page size - {} - page number - {} - number of records retrieved - {}", pageSize, pageNumber, loanDetailInfoList.size());
        //convert entity to response
        List<LoanDetailInfoRp> loanDetailInfoRpList = loanDetailInfoList.stream().map(this::mapObjectToLoanDetailInfoRp).toList();
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("totalRecord", loanDetailInfoPage.getTotalElements());
        dataResponse.put("loanDetailInfoRpList", loanDetailInfoRpList);
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getAllLoanIsActive(Integer pageNumber, Integer pageSize, String transactionId, String cifCode) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<LoanDetailActiveHistoryProjection> loanDetailInfoActiveHistoryPage = loanDetailInfoRepository.findAllLoanActiveHistoryByCifCode(cifCode, pageable);
        List<LoanDetailActiveHistoryProjection> loanDetailActiveHistoryProjectionsList = loanDetailInfoActiveHistoryPage.getContent();
        log.debug("Total element expect is active : {}", loanDetailInfoActiveHistoryPage.getTotalElements());
        log.info("Page size - {} - page number - {} - number of records retrieved - {}", pageSize, pageNumber, loanDetailActiveHistoryProjectionsList.size());
        //convert entity to response
        List<LoanDetailInfoActiveRp> loanDetailInfoRpList = loanDetailActiveHistoryProjectionsList.stream().map(this::mapObjectToLoanDetailInfoActiveRp).toList();
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("totalRecord", loanDetailInfoActiveHistoryPage.getTotalElements());
        dataResponse.put("loanDetailInfoRpList", loanDetailInfoRpList);
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getDetailByLoanInfoDetailId(String loanInfoId, String transactionId) {
        LoanDetailInfo loanDetailInfo = loanDetailRepaymentScheduleService.getLoanDetailInfoById(loanInfoId, transactionId);
        List<RepaymentScheduleProjection> paymentScheduleList = paymentScheduleService.getListPaymentScheduleByLoanDetailInfo(loanDetailInfo.getId(), transactionId);
        List<PaymentScheduleRp> paymentScheduleRpList = paymentScheduleList.stream().map(MapEntityToDto::mapToPaymentScheduleRp).toList();
        String amountLoanRemaining = Util.formatToVND(loanDetailInfoRepository.getAmountRemainingByLoanDetailInfoId(loanDetailInfo.getId()));
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("paymentScheduleRpList", paymentScheduleRpList);
        dataResponse.put("amountLoanRemaining", amountLoanRemaining);
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getLoanReportByCifCode(String cifCode, String transactionId) {
//        LoanDetailInfo loanDetailInfo=loanDetailInfoRepository.findAllLoanActiveHistoryByCifCode()
        return null;
    }

    @Override
    public DataResponseWrapper<Object> cancelLoanRequest(String id, String transactionId) {
        LoanDetailInfo loanDetailInfo = loanDetailRepaymentScheduleService.getLoanDetailInfoById(id, transactionId);
        if (loanDetailInfo.getRequestStatus().equals(RequestStatus.PENDING)) {
            String messageLog = String.format(MessageData.REQUEST_STATUS_LOAN_NOT_PENDING.getMessageLog(), loanDetailInfo.getId(), loanDetailInfo.getRequestStatus().name());
            log.debug(MessageData.MESSAGE_LOG, transactionId, messageLog);
            throw new DataNotValidException(MessageData.REQUEST_STATUS_LOAN_NOT_PENDING);
        }
        loanDetailInfo.setRequestStatus(RequestStatus.CANCEL);
        loanDetailInfoRepository.save(loanDetailInfo);
        return DataResponseWrapper.builder()
                .data(mapObjectToLoanDetailInfoRp(loanDetailInfo))
                .message(util.getMessageFromMessageSource(MessageData.UPDATE_SUCCESSFULLY.getKeyMessage()))
                .status(MessageData.UPDATE_SUCCESSFULLY.getCode())
                .build();
    }

    @Override
    public DataResponseWrapper<Object> earlyPaymentLoan(String transactionId, String loanInfoId) {
        LoanDetailInfo loanDetailInfo = loanDetailRepaymentScheduleService.getLoanDetailInfoById(loanInfoId, transactionId);
        DisbursementInfoHistory disbursementInfoHistory = loanDetailInfo.getDisbursementInfoHistory();
        if (disbursementInfoHistory == null || !loanDetailInfo.getRequestStatus().equals(RequestStatus.APPROVED)) {
            //todo: write log and exception
            throw new ServerErrorException();
        }
        try {
            FinancialInfo financialInfo = loanDetailInfo.getFinancialInfo();
            AccountInfoDTO loanAccountInfoDTO = accountDubboService.getLoanAccountDTO(disbursementInfoHistory.getLoanAccountId());
            AccountInfoDTO bankingAccountInfoDto = accountDubboService.getBankingAccount(financialInfo.getCifCode());
            log.info("balance loan account - {} account id - {} ",
                    loanDetailInfo.getDisbursementInfoHistory().getLoanDetailInfo(),
                    loanAccountInfoDTO.getCurrentAccountBalance());
            log.info("balance banking account - {}", bankingAccountInfoDto.getCurrentAccountBalance());
            Timestamp currentDate = new Timestamp(System.currentTimeMillis());
            //get list payment period not due
            List<PaymentSchedule> paymentScheduleListQueryResult = paymentScheduleService.getListPaymentScheduleByDueDateAfterCurrentDate(loanInfoId, currentDate);
            //get current payment period
            PaymentSchedule paymentScheduleCurrent = paymentScheduleListQueryResult.get(0);
            BigDecimal interestCurrentPeriod = getAmountInterestCurrentPeriod(currentDate, paymentScheduleCurrent, loanAccountInfoDTO);
            BigDecimal amountFined = loanAccountInfoDTO.getCurrentAccountBalance()
                    .multiply(BigDecimal.valueOf(2))
                    .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
            BigDecimal amountNeedPayment = loanAccountInfoDTO.getCurrentAccountBalance()
                    .add(amountFined)
                    .add(interestCurrentPeriod);

            if (bankingAccountInfoDto.getCurrentAccountBalance().compareTo(amountNeedPayment) < 0) {
                throw new ServerErrorException();
            }
            CreateLoanPaymentTransactionDTO loanAmountPaymentTransactionDTO = createLoanPaymentTransactionDTO(loanAccountInfoDTO, bankingAccountInfoDto, financialInfo, loanAccountInfoDTO.getCurrentAccountBalance());
            CreateLoanPaymentTransactionDTO loanInterestPaymentTransactionDTO = createLoanPaymentTransactionDTO(loanAccountInfoDTO, bankingAccountInfoDto, financialInfo, interestCurrentPeriod);
            CreateLoanPaymentTransactionDTO loanPenaltyTransactionDTO = createLoanPaymentTransactionDTO(loanAccountInfoDTO, bankingAccountInfoDto, financialInfo, amountFined);
            //call transaction service to add amount interest
            createLoanTransactionDTO(loanAccountInfoDTO, financialInfo, amountFined, PaymentType.INTEREST.name(), "Add early interest");
            //call transaction service to add fee fined early payment
            createLoanTransactionDTO(loanAccountInfoDTO, financialInfo, interestCurrentPeriod, PaymentType.PENALTY.name(), "Add early fee fined");

            List<RepaymentHistory> repaymentHistoryList = new ArrayList<>();
            TransactionLoanResultDTO transactionPaymentLoanResultDTO = transactionDubboService.createLoanPaymentTransaction(loanAmountPaymentTransactionDTO);
            TransactionLoanResultDTO transactionPaymentInterestResultDTO = transactionDubboService.createLoanPaymentTransaction(loanInterestPaymentTransactionDTO);
            TransactionLoanResultDTO transactionPaymentPenaltyResultDTO = transactionDubboService.createLoanPaymentTransaction(loanPenaltyTransactionDTO);

            RepaymentHistory repaymentHistory = createRepaymentHistory(paymentScheduleCurrent,
                    transactionPaymentInterestResultDTO,
                    PaymentTransactionType.INTEREST_PAYMENT,
                    loanInterestPaymentTransactionDTO.getAmount());
            repaymentHistoryList.add(repaymentHistory);
            loanDetailInfo.setLoanStatus(LoanStatus.PAID_OFF);
            disbursementInfoHistory.setLoanDate(DateUtil.getCurrentTimeUTC7());
            createAndSaveInformationPaymentLoan(repaymentHistoryList,
                    paymentScheduleListQueryResult,
                    amountFined,
                    interestCurrentPeriod,
                    transactionPaymentLoanResultDTO,
                    transactionPaymentPenaltyResultDTO);
            AccountInfoDTO accountInfoDTO = accountDubboService.updateAccountStatus(loanAccountInfoDTO.getAccountNumber(), ObjectStatus.CLOSED);
            if (accountInfoDTO.getStatusAccount().compareTo(ObjectStatus.CLOSED) != 0) {
                throw new ServerErrorException();
            }
            loanDetailInfoRepository.save(loanDetailInfo);
            return DataResponseWrapper.builder()
                    .build();
        } catch (ServerErrorException | DubboException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            //todo: rollback transaction
            throw new ServerErrorException(transactionId, MessageData.RESOURCE_MAPPING_MESSAGE_ERROR.getCode());
        }
    }

    private static @NotNull RepaymentHistory createRepaymentHistory(PaymentSchedule paymentScheduleCurrent,
                                                                    TransactionLoanResultDTO transactionPaymentInterestResultDTO,
                                                                    PaymentTransactionType paymentTransactionType,
                                                                    BigDecimal amount) {
        RepaymentHistory repaymentHistory = new RepaymentHistory();
        repaymentHistory.setPaymentSchedule(paymentScheduleCurrent);
        repaymentHistory.setTransactionId(transactionPaymentInterestResultDTO.getTransactionId());
        repaymentHistory.setNote(paymentTransactionType.name());
        repaymentHistory.setPaymentType(paymentTransactionType);
        repaymentHistory.setAmountPayment(amount);
        return repaymentHistory;
    }

    private void createAndSaveInformationPaymentLoan(List<RepaymentHistory> repaymentHistoryList,
                                                     List<PaymentSchedule> paymentScheduleListQueryResult,
                                                     BigDecimal amountFined,
                                                     BigDecimal interestCurrentPeriod,
                                                     TransactionLoanResultDTO transactionPaymentLoanResultDTO,
                                                     TransactionLoanResultDTO transactionPaymentPenaltyResultDTO
    ) {
        List<LoanPenalties> loanPenaltiesList = new ArrayList<>();
        List<PaymentSchedule> paymentScheduleListUpdate = paymentScheduleListQueryResult
                .stream()
                .peek(paymentSchedule -> {
                    paymentSchedule.setAmountInterestRate(BigDecimal.ZERO);
                    paymentSchedule.setIsPaid(true);
                    paymentSchedule.setIsPaidInterest(true);
                    paymentSchedule.setPaymentInterestDate(DateUtil.getCurrentTimeUTC7());
                    paymentSchedule.setPaymentScheduleDate(DateUtil.getCurrentTimeUTC7());
                    paymentSchedule.setStatus(DeftRepaymentStatus.EARLY_PAID);
                    RepaymentHistory paymentPrincipalHistory = createRepaymentHistory(paymentSchedule,
                            transactionPaymentLoanResultDTO,
                            PaymentTransactionType.PRINCIPAL_PAYMENT,
                            paymentSchedule.getAmountRepayment());
                    //create fee penalty for early payment periods
                    BigDecimal amountFinedPeriod = amountFined.divide(BigDecimal.valueOf(paymentScheduleListQueryResult.size()), RoundingMode.HALF_UP);
                    LoanPenalties loanPenalties = createLoanPenalties(paymentSchedule, amountFined, paymentScheduleListQueryResult.size());
                    RepaymentHistory paymentPenaltyHistory = createRepaymentHistory(paymentSchedule,
                            transactionPaymentPenaltyResultDTO,
                            PaymentTransactionType.EARLY_REPAYMENT_PENALTY,
                            amountFinedPeriod);
                    paymentPenaltyHistory.setAmountPayment(amountFinedPeriod);
                    loanPenaltiesList.add(loanPenalties);
                    repaymentHistoryList.add(paymentPenaltyHistory);
                    repaymentHistoryList.add(paymentPrincipalHistory);
                }).toList();
        //update interest for current payment period
        paymentScheduleListUpdate.get(0).setAmountInterestRate(interestCurrentPeriod);
        paymentScheduleService.saveAndFlushAllPaymentScheduleAndLoanPenaltiesAndRepaymentHistory(paymentScheduleListUpdate, loanPenaltiesList, repaymentHistoryList);
    }


    @Override
    public DataResponseWrapper<Object> getEarlyPaymentPenaltyFee(String loanInfoId, String transactionId) {
        log.info("[{}] Start processing early payment penalty fee for loanInfoId: {}", transactionId, loanInfoId);

        // Retrieve loan details
        LoanDetailInfo loanDetailInfo = loanDetailRepaymentScheduleService.getLoanDetailInfoById(loanInfoId, transactionId);
        if (loanDetailInfo == null) {
            log.error("[{}] Loan detail not found for loanInfoId: {}", transactionId, loanInfoId);
            throw new DataNotValidException(MessageData.LOAN_DETAIL_INFO_NOT_FOUND);
        }
        log.info("[{}] Successfully retrieved loan details", transactionId);

        // Retrieve loan account information
        AccountInfoDTO loanAccountInfoDTO = accountDubboService.getLoanAccountDTO(loanDetailInfo.getDisbursementInfoHistory().getLoanAccountId());
        log.info("[{}] Loan account balance: {}", transactionId, loanAccountInfoDTO.getCurrentAccountBalance());

        // Get the first upcoming payment schedule after the current date
        Timestamp currentDate = new Timestamp(System.currentTimeMillis());
        PaymentSchedule paymentScheduleCurrent = paymentScheduleService.getFirstPaymentScheduleByDueDateAfterCurrentDate(loanInfoId, currentDate);
        if (paymentScheduleCurrent == null) {
            log.error("[{}] No upcoming payment schedule found for loanInfoId: {}", transactionId, loanInfoId);
            throw new DataNotValidException(MessageData.PAYMENT_SCHEDULE_NOT_FOUND);
        }
        log.info("[{}] Found upcoming payment schedule with due date: {}", transactionId, paymentScheduleCurrent.getDueDate());

        // Calculate interest for the current period
        BigDecimal interestCurrentPeriod = getAmountInterestCurrentPeriod(currentDate, paymentScheduleCurrent, loanAccountInfoDTO);
        log.info("[{}] Calculated interest for the current period: {}", transactionId, interestCurrentPeriod);

        // Calculate the early payment penalty fee (2% of the remaining loan balance)
        BigDecimal amountFined = loanAccountInfoDTO.getCurrentAccountBalance()
                .multiply(BigDecimal.valueOf(2)) // 2% penalty
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        log.info("[{}] Calculated early payment penalty fee (2% of balance): {}", transactionId, amountFined);

        // Calculate the total amount required for early repayment (principal + penalty fee + interest)
        BigDecimal amountNeedPayment = loanAccountInfoDTO.getCurrentAccountBalance()
                .add(amountFined)
                .add(interestCurrentPeriod);
        log.info("[{}] Total amount required for early payment: {}", transactionId, amountNeedPayment);

        // Prepare response data
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("loanDetailInfo", mapObjectToLoanDetailInfoRp(loanDetailInfo)); // Convert loan details
        dataResponse.put("amountFined", Util.formatToVND(amountFined.stripTrailingZeros())); // Penalty fee
        dataResponse.put("interestCurrentPeriod", Util.formatToVND(interestCurrentPeriod)); // Interest for the current period
        dataResponse.put("amountRemainingLoan", Util.formatToVND(loanAccountInfoDTO.getCurrentAccountBalance())); // Remaining loan balance
        dataResponse.put("amountNeedPayment", Util.formatToVND(amountNeedPayment)); // Total amount required for early payment

        // Return successful response
        log.info("[{}] Successfully processed early payment penalty fee", transactionId);
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }


    private static @NotNull BigDecimal getAmountInterestCurrentPeriod(Timestamp currentDate, PaymentSchedule paymentScheduleCurrent, AccountInfoDTO loanAccountInfoDTO) {
        LocalDate currentDateLocal = currentDate.toInstant().atZone(ZoneId.of(DateUtil.ZONE_ID_VN_HCM)).toLocalDate();
        LocalDate dueDateLocal = paymentScheduleCurrent.getDueDate().toInstant().atZone(ZoneId.of(DateUtil.ZONE_ID_VN_HCM)).toLocalDate();
        long differenceInDays = ChronoUnit.DAYS.between(currentDateLocal, dueDateLocal);
        int daysInMonth = currentDate.toInstant().atZone(ZoneId.of(DateUtil.ZONE_ID_VN_HCM)).toLocalDate().lengthOfMonth();
        return paymentScheduleCurrent.getAmountInterestRate().multiply(
                BigDecimal.valueOf(differenceInDays).divide(BigDecimal.valueOf(daysInMonth), RoundingMode.HALF_UP)
        );
    }

    private static @NotNull DisbursementInfoHistory getDisbursementInfoHistory(LoanDetailInfo loanDetailInfo,
                                                                               LoanAccountInfoDTO loanAccountInfoDTO,
                                                                               TransactionLoanResultDTO transactionLoanResultDTO) {
        DisbursementInfoHistory disbursementInfoHistory = new DisbursementInfoHistory();
        disbursementInfoHistory.setLoanDetailInfo(loanDetailInfo);
        FinancialInfo financialInfo = loanDetailInfo.getFinancialInfo();
        disbursementInfoHistory.setIncome(financialInfo.getIncome());
        disbursementInfoHistory.setCreditScore(financialInfo.getCreditScore());
        disbursementInfoHistory.setIncomeSource(financialInfo.getIncomeSource());
        disbursementInfoHistory.setIncomeType(financialInfo.getIncomeType());
        disbursementInfoHistory.setDebtStatus(financialInfo.getDebtStatus());
        disbursementInfoHistory.setLastUpdatedCreditReview(financialInfo.getLastUpdatedCreditReview());
        disbursementInfoHistory.setLoanAccountId(loanAccountInfoDTO.getAccountLoanId());
        disbursementInfoHistory.setAmountDisbursement(loanDetailInfo.getLoanAmount());
        disbursementInfoHistory.setLoanDate(DateUtil.getDateAfterNDay(1));
        disbursementInfoHistory.setDouDate(DateUtil.getDateAfterNMonths(loanDetailInfo.getLoanTerm()));
        return disbursementInfoHistory;
    }

    private CustomerLoanDetailInfoRp mapObjectToCustomerLoanDetailInfoDto(LoanDetailInfo loanDetailInfo, CustomerDetailDTO customerDetailDTO) {
        CustomerLoanDetailInfoRp customerLoanDetailInfoRp = new CustomerLoanDetailInfoRp();
        customerLoanDetailInfoRp.setCustomerId(customerDetailDTO.getCustomerId());
        customerLoanDetailInfoRp.setFormDeftRepayment(loanDetailInfo.getFormDeftRepayment().name());
        customerLoanDetailInfoRp.setFullName(customerDetailDTO.getFullName());
        customerLoanDetailInfoRp.setIdentityCard(customerDetailDTO.getIdentityCard());
        customerLoanDetailInfoRp.setInterestRate(loanDetailInfo.getInterestRate());
        customerLoanDetailInfoRp.setLoanAmount(Util.formatToVND(loanDetailInfo.getLoanAmount()));
        customerLoanDetailInfoRp.setLoanDetailInfoId(loanDetailInfo.getId());
        customerLoanDetailInfoRp.setLoanProductName(loanDetailInfo.getLoanProductId().getNameProduct());
        customerLoanDetailInfoRp.setLoanTerm(loanDetailInfo.getLoanTerm());
        customerLoanDetailInfoRp.setPhone(customerDetailDTO.getPhone());
        customerLoanDetailInfoRp.setUnit(loanDetailInfo.getUnit().name());
        customerLoanDetailInfoRp.setCreatedTime(DateUtil.format(DateUtil.DD_MM_YYY_HH_MM_SLASH, loanDetailInfo.getCreatedDate()));
        customerLoanDetailInfoRp.setLoanStatus(loanDetailInfo.getLoanStatus().name());
        return customerLoanDetailInfoRp;
    }

    private LoanDetailInfoRp mapObjectToLoanDetailInfoRp(LoanDetailInfo loanDetailInfo) {
        LoanDetailInfoRp loanDetailInfoRp = new LoanDetailInfoRp();
        loanDetailInfoRp.setLoanDetailInfoId(loanDetailInfo.getId());
        loanDetailInfoRp.setLoanAmount(Util.formatToVND(loanDetailInfo.getLoanAmount()));
        loanDetailInfoRp.setLoanProductName(loanDetailInfo.getLoanProductId().getNameProduct());
        loanDetailInfoRp.setLoanTerm(loanDetailInfo.getLoanTerm());
        loanDetailInfoRp.setUnit(loanDetailInfo.getUnit().name());
        loanDetailInfoRp.setInterestRate(loanDetailInfo.getInterestRate());
        loanDetailInfoRp.setCreatedDate(DateUtil.format(DateUtil.DD_MM_YYY_HH_MM_SLASH, loanDetailInfo.getCreatedDate()));
        loanDetailInfoRp.setLoanStatus(loanDetailInfo.getLoanStatus().name());
        DisbursementInfoHistory disbursementInfoHistory = loanDetailInfo.getDisbursementInfoHistory();
        if (disbursementInfoHistory != null) {
            loanDetailInfoRp.setDouDate(DateUtil.format(DateUtil.DD_MM_YYY_HH_MM_SLASH, disbursementInfoHistory.getDouDate()));
            loanDetailInfoRp.setDateDisbursement(DateUtil.format(DateUtil.DD_MM_YYY_HH_MM_SLASH, disbursementInfoHistory.getLoanDate()));
        }
        return loanDetailInfoRp;
    }

    private LoanDetailInfoRp mapObjectToLoanDetailInfoRp(LoanInfoDetailProjection loanInfoDetailProjection) {
        LoanDetailInfoRp loanDetailInfoRp = new LoanDetailInfoRp();
        loanDetailInfoRp.setLoanDetailInfoId(loanInfoDetailProjection.getId());
        loanDetailInfoRp.setLoanProductName(loanInfoDetailProjection.getNameLoanProduct());
        loanDetailInfoRp.setLoanAmount(Util.formatToVND(loanInfoDetailProjection.getLoanAmount()));
        loanDetailInfoRp.setLoanTerm(loanInfoDetailProjection.getLoanTerm());
        loanDetailInfoRp.setInterestRate(loanInfoDetailProjection.getInterestRate());
        loanDetailInfoRp.setRequestStatus(loanInfoDetailProjection.getRequestStatus());
        loanDetailInfoRp.setLoanStatus(loanInfoDetailProjection.getLoanStatus());
        loanDetailInfoRp.setCreatedDate(DateUtil.format(DateUtil.YYYY_MM_DD_HH_MM_SS, loanInfoDetailProjection.getCreatedDate()));
        return loanDetailInfoRp;
    }

    private static CreateLoanPaymentTransactionDTO createLoanPaymentTransactionDTO(AccountInfoDTO loanAccountInfoDTO, AccountInfoDTO bankingAccountInfoDto, FinancialInfo financialInfo, BigDecimal amountNeedPayment) {
        return CreateLoanPaymentTransactionDTO.builder()
                .loanAccount(loanAccountInfoDTO.getAccountNumber())
                .paymentAccount(bankingAccountInfoDto.getAccountNumber())
                .note(DeftRepaymentStatus.EARLY_PAID.name())
                .cifCode(financialInfo.getCifCode())
                .isCashPayment(false)
                .description(DeftRepaymentStatus.EARLY_PAID.name())
                .amount(amountNeedPayment)
                .build();
    }

    private static @NotNull LoanPenalties createLoanPenalties(PaymentSchedule paymentSchedule, BigDecimal amountFined, int remainTerm) {
        LoanPenalties loanPenalties = new LoanPenalties();
        loanPenalties.setPaymentSchedule(paymentSchedule);
        loanPenalties.setFinedReason(DeftRepaymentStatus.EARLY_PAID.name());
        loanPenalties.setFinedAmount(amountFined.divide(BigDecimal.valueOf(remainTerm), RoundingMode.HALF_UP));
        loanPenalties.setIsPaid(true);
        loanPenalties.setFinedDate(DateUtil.getCurrentTimeUTC7());
        loanPenalties.setFinedPaymentDate(DateUtil.getCurrentTimeUTC7());
        return loanPenalties;
    }

    private void createLoanTransactionDTO(AccountInfoDTO loanAccountInfoDTO,
                                          FinancialInfo financialInfo,
                                          BigDecimal amountFined,
                                          String note,
                                          String description
    ) {
        CreateLoanTransactionDTO createLoanTransactionDTO = new CreateLoanTransactionDTO();
        createLoanTransactionDTO.setLoanAccount(loanAccountInfoDTO.getAccountNumber());
        createLoanTransactionDTO.setNote(note);
        createLoanTransactionDTO.setDescription(description);
        createLoanTransactionDTO.setCifCode(financialInfo.getCifCode());
        createLoanTransactionDTO.setAmount(amountFined);
        transactionDubboService.createLoanTransaction(createLoanTransactionDTO);
    }

    public @NonNull LoanDetailInfoActiveRp mapObjectToLoanDetailInfoActiveRp(LoanDetailActiveHistoryProjection projection) {
        LoanDetailInfoActiveRp dto = new LoanDetailInfoActiveRp();
        dto.setLoanInfoId(projection.getId() != null ? String.valueOf(projection.getId()) : null);
        dto.setDueDate(projection.getDueDate() != null ? DateUtil.format(DateUtil.FULL_DATE, projection.getDueDate()) : null);
        dto.setNextRepaymentDate(projection.getDueDateRepaymentTerm() != null ? DateUtil.format(DateUtil.FULL_DATE, projection.getDueDateRepaymentTerm()) : null);
        dto.setLoanDate(projection.getLoanDate() != null ? DateUtil.format(DateUtil.FULL_DATE, projection.getLoanDate()) : null);
        dto.setLoanAmount(projection.getAmountDisbursement() != null ? Util.formatToVND(projection.getAmountDisbursement()) : null);
        // compare loan amount remain: loanAmount - amountDeftPaid
        if (projection.getAmountDisbursement() != null && projection.getAmountDeftPaid() != null) {
            dto.setLoanAmountRemaining(Util.formatToVND(projection.getAmountDisbursement().subtract(projection.getAmountDeftPaid())));
        } else {
            dto.setLoanAmountRemaining(Util.formatToVND(BigDecimal.ZERO));
        }

        dto.setNextLoanAmountRepayment(projection.getAmountRepayment() != null ? Util.formatToVND(projection.getAmountRepayment()) : null);
        dto.setLoanProductName(projection.getNameProduct());
        dto.setLoanTermName(projection.getNameTerm());
        dto.setLoanTerm(projection.getLoanTerm());
        return dto;
    }
}
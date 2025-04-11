package org.demo.loanservice.services.impl;

import com.system.common_library.dto.notifcation.rabbitMQ.LoanFinancialReviewSuccessNoti;
import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.user.CustomUserDetail;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.ObjectStatus;
import com.system.common_library.exception.DubboException;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.common.DateUtil;
import org.demo.loanservice.common.MessageData;
import org.demo.loanservice.common.MessageValue;
import org.demo.loanservice.common.Util;
import org.demo.loanservice.controllers.exception.DataNotFoundException;
import org.demo.loanservice.controllers.exception.DataNotValidException;
import org.demo.loanservice.controllers.exception.ServerErrorException;
import org.demo.loanservice.dto.CICResponse;
import org.demo.loanservice.dto.enumDto.ApplicableObjects;
import org.demo.loanservice.dto.enumDto.DocumentType;
import org.demo.loanservice.dto.enumDto.LoanCategory;
import org.demo.loanservice.dto.enumDto.RequestStatus;
import org.demo.loanservice.dto.enumDto.Unit;
import org.demo.loanservice.dto.projection.LoanAmountInfoProjection;
import org.demo.loanservice.dto.projection.StatisticalLoanProjection;
import org.demo.loanservice.dto.request.ApproveFinancialInfoRq;
import org.demo.loanservice.dto.request.FinancialInfoRq;
import org.demo.loanservice.dto.response.FinancialDetailRp;
import org.demo.loanservice.dto.response.FinancialInfoRp;
import org.demo.loanservice.dto.response.LegalDocumentRp;
import org.demo.loanservice.dto.response.PieChartData;
import org.demo.loanservice.entities.FinancialInfo;
import org.demo.loanservice.entities.LegalDocuments;
import org.demo.loanservice.repositories.FinancialInfoRepository;
import org.demo.loanservice.repositories.LegalDocumentsRepository;
import org.demo.loanservice.repositories.LoanDetailInfoRepository;
import org.demo.loanservice.services.IFinancialInfoService;
import org.demo.loanservice.services.INotificationService;
import org.demo.loanservice.wiremockService.CICService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialInfoServiceImpl implements IFinancialInfoService {
    private final FinancialInfoRepository financialInfoRepository;
    private final LegalDocumentsRepository legalDocumentsRepository;
    private final LoanDetailInfoRepository loanDetailInfoRepository;
    @DubboReference
    private CustomerDubboService customerDubboService;
    @DubboReference
    private AccountDubboService accountDubboService;
    private final Logger log = LogManager.getLogger(FinancialInfoServiceImpl.class);
    private final INotificationService notificationService;
    private final CICService cicService;
    private final Util util;

    @Override
    @Transactional
    public DataResponseWrapper<Object> saveInfoIndividualCustomer(FinancialInfoRq financialInfoRq, List<MultipartFile> incomeVerificationDocuments, String transactionId) {
        CustomUserDetail currentUserSession = util.getCurrentUserSession();
        String cifCode=currentUserSession.getCifCode();
        List<FinancialInfo> financialInfoList = financialInfoRepository
                .findAllByRequestStatusInAndIsDeletedFalseAndCifCodeAndIsExpiredFalse(
                        List.of(
                                RequestStatus.APPROVED,
                                RequestStatus.PENDING),
                        cifCode);
        if (!financialInfoList.isEmpty()) {
            log.info(MessageData.MESSAGE_LOG, MessageData.FINANCIAL_INFO_IS_REGISTERED.getMessageLog(), transactionId);
            throw new DataNotValidException(MessageData.FINANCIAL_INFO_IS_REGISTERED);
        }
        CustomerDetailDTO customerInfo;
        // Fetch customer information by CIF code
        try {
            //Validate customer information
            customerInfo = customerDubboService.getCustomerByCifCode(cifCode);
            if (customerInfo == null) {
                log.info(MessageData.MESSAGE_LOG, transactionId, MessageData.CUSTOMER_ACCOUNT_NOT_FOUND.getMessageLog());
                throw new DataNotFoundException(MessageData.CUSTOMER_ACCOUNT_NOT_FOUND);
            }
            log.debug("Customer status: {}", customerInfo.getStatus());
            if (!customerInfo.getStatus().equals(ObjectStatus.ACTIVE)) {
                log.info(MessageData.MESSAGE_LOG, MessageData.CUSTOMER_ACCOUNT_NOT_ACTIVE.getMessageLog(), transactionId);
                throw new DataNotValidException(MessageData.CUSTOMER_ACCOUNT_NOT_ACTIVE);
            }


//         Check if the banking account is active
            AccountInfoDTO bankingAccountInfoDTO = accountDubboService.getBankingAccount(cifCode);
            if (bankingAccountInfoDTO == null) {
                log.error("transactionId : {} :: Execute error while get banking account. cif code: {}", transactionId, cifCode);
                throw new DataNotValidException(MessageData.BANKING_ACCOUNT_NOT_EXITS);
            }
            if (!bankingAccountInfoDTO.getStatusAccount().equals(ObjectStatus.ACTIVE)) {
                log.info(MessageData.MESSAGE_LOG, MessageData.BANKING_ACCOUNT_NOT_ACTIVE.getMessageLog(), transactionId);
                throw new DataNotValidException(MessageData.BANKING_ACCOUNT_NOT_ACTIVE);
            }

            // Fetch credit score from CIC service (To-Do: Update input parameters dynamically)
            CICResponse cicResponse = cicService.getCreditScore("079123456789", "vu duc thang", "", "0123456789");
            // Create and save financial information record
            FinancialInfo financialInfo = new FinancialInfo();
            financialInfo.setCustomerId(customerInfo.getCustomerId());
            financialInfo.setCustomerNumber(customerInfo.getCustomerNumber());
            financialInfo.setIncome(financialInfoRq.getIncome().stripTrailingZeros().toPlainString());
            financialInfo.setUnit(Unit.valueOf(financialInfoRq.getUnit()));
            financialInfo.setIncomeSource(financialInfoRq.getIncomeSource());
            financialInfo.setIncomeType(financialInfoRq.getIncomeType());
            financialInfo.setRequestStatus(RequestStatus.PENDING);
            financialInfo.setCreditScore(cicResponse.getCreditScore());
            financialInfo.setDebtStatus(cicResponse.getDebtStatus());
            financialInfo.setIsExpired(false);
            financialInfo.setExpiredDate(new Date(DateUtil.getDateOfAfterNMonth(1).getTime())); // Set expiry date to 6 months later
            financialInfo.setIsDeleted(false);
            financialInfo.setLastUpdatedCreditReview(DateUtil.convertStringToTimeStamp(cicResponse.getLastUpdated()));
            financialInfo.setApplicableObjects(ApplicableObjects.INDIVIDUAL_CUSTOMER);
            financialInfo.setLoanAmountMax(BigDecimal.ZERO);
            financialInfo.setCifCode(customerInfo.getCifCode());
            financialInfoRepository.save(financialInfo);

            // Process income verification documents
            List<LegalDocuments> legalDocumentsList = new ArrayList<>();
            incomeVerificationDocuments.forEach(multipartFile -> {
                LegalDocuments legalDocument = new LegalDocuments();
                legalDocument.setCifCode(currentUserSession.getCifCode());
                legalDocument.setDescription("Financial information document");
                legalDocument.setIsDeleted(false);
                legalDocument.setExpirationDate(new Date(DateUtil.getDateOfAfterNMonth(3).getTime())); // Expiry date: 3 months later
                legalDocument.setDocumentType(DocumentType.LOAN_DOCUMENT);
                legalDocument.setUrlDocument(multipartFile.getOriginalFilename()); // ToDo: Upload file to S3 and get URL
                legalDocument.setRequestStatus(RequestStatus.APPROVED);
                legalDocumentsList.add(legalDocument);
            });
            legalDocumentsRepository.saveAll(legalDocumentsList);

            // Associate legal documents with financial information
            legalDocumentsList.forEach(legalDocument -> legalDocumentsRepository.insertFinancialInfoDocument(financialInfo.getId(), legalDocument.getId()));

            // Return response
            return DataResponseWrapper.builder()
                    .data(financialInfo.getId())
                    .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                    .message("Your financial information has been successfully registered and is pending review!") // To-Do: Handle response message dynamically
                    .build();
        } catch (DubboException e) {
            log.info(MessageData.MESSAGE_LOG, transactionId, e.getMessage());
            throw e; // Handle server errors gracefully
        } catch (Exception e) {
            log.error(MessageData.MESSAGE_LOG, transactionId, e.getMessage());
            throw new ServerErrorException();
        }
    }


    @Override
    public DataResponseWrapper<Object> getAllInfoIsByStatus(Integer pageNumber, Integer pageSize,
                                                            String status, String transactionId) {

        Pageable page = PageRequest.of(pageNumber, pageSize);
        Page<FinancialInfo> financialInfoPage = financialInfoRepository.findAllByIsDeletedAndRequestStatus(false, RequestStatus.valueOf(status), page);
        List<FinancialInfo> financialInfoList = financialInfoPage.getContent();
        List<String> listCifCode = financialInfoList.stream().map(FinancialInfo::getCifCode).toList();
        List<CustomerDetailDTO> customerDetailDTOList = customerDubboService.getListCustomerByCifCode(listCifCode);
        log.debug("Size of customer detail dto list : {}", customerDetailDTOList.size());
        // Convert list to map
        Map<String, CustomerDetailDTO> customerDetailDTOMap = customerDetailDTOList.stream()
                .collect(Collectors.toMap(
                        CustomerDetailDTO::getCustomerId,
                        Function.identity(),
                        (existing, replacement) -> existing // (void duplicate key issue)Keep the existing entry in case of duplicate
                ));
        //convert to response to dto response
        List<FinancialInfoRp> financialInfoRpList = financialInfoList
                .stream()
                .map(financialInfo -> {
                    CustomerDetailDTO customerDetailDTO = customerDetailDTOMap.get(financialInfo.getCustomerId());
                    return convertToFinancialInfoRp(financialInfo, customerDetailDTO, false);
                })
                .toList();

        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("financialInfoRpList", financialInfoRpList);
        dataResponse.put("totalRecords", financialInfoPage.getTotalElements());
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message("")//todo: handler response message
                .build();
    }

    @Override
    @Cacheable(value = "financial-info", key = "#id", unless = "#result == null")
    public DataResponseWrapper<Object> getDetailInfoById(String id, String transactionId) {
        FinancialInfo financialInfo = financialInfoRepository.findByIdAndIsDeleted(id, false)
                .orElseThrow(
                        () -> {
                            log.info(MessageData.MESSAGE_LOG_NOT_FOUND_DATA, transactionId, MessageData.FINANCIAL_INFO_NOT_FOUND.getMessageLog(), id);
                            return new DataNotFoundException(MessageData.FINANCIAL_INFO_NOT_FOUND);//todo
                        }
                );
        FinancialInfoRp financialInfoRp = convertToFinancialInfoRp(financialInfo, null, true);
        return DataResponseWrapper.builder()
                .data(financialInfoRp)
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message("")//todo: handler response message
                .build();
    }

    @Override
    @CacheEvict(value = "financial-info", key = "#financialInfoRq.financialInfoId")
    public DataResponseWrapper<Object> approveFinancialInfo(ApproveFinancialInfoRq financialInfoRq, String
            transactionId) {
        FinancialInfo financialInfo = financialInfoRepository.findByIdAndIsDeleted(financialInfoRq.getFinancialInfoId(), false)
                .orElseThrow(
                        () -> {
                            log.info(MessageData.MESSAGE_LOG_NOT_FOUND_DATA, transactionId, MessageData.FINANCIAL_INFO_NOT_FOUND.getMessageLog(), financialInfoRq.getFinancialInfoId());
                            return new DataNotFoundException(MessageData.FINANCIAL_INFO_NOT_FOUND);
                        }
                );
        financialInfo.setRequestStatus(RequestStatus.valueOf(financialInfoRq.getStatusFinancialInfo()));
        financialInfo.setNote(financialInfoRq.getNote());
        financialInfo.setLoanAmountMax(financialInfoRq.getLoanAmountLimit());
        financialInfo.setExpiredDate(new Date(DateUtil.getDateOfAfterNMonth(6).getTime()));
        financialInfoRepository.saveAndFlush(financialInfo);
        //create and push notify
        if (financialInfoRq.getStatusFinancialInfo().equals(RequestStatus.APPROVED.name())) {
            LoanFinancialReviewSuccessNoti loanFinancialReviewSuccessNoti = new LoanFinancialReviewSuccessNoti();
            loanFinancialReviewSuccessNoti.setApprovedLimit(financialInfoRq.getLoanAmountLimit());
            loanFinancialReviewSuccessNoti.setCustomerCIF(financialInfo.getCifCode());
            loanFinancialReviewSuccessNoti.setExpiryDate(financialInfo.getExpiredDate().toLocalDate());
            notificationService.sendNotificationLoanFinancialReviewSuccess(loanFinancialReviewSuccessNoti);
        }
        return DataResponseWrapper.builder()
                .data(financialInfo.getId())
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message("")
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getStatisticalLoan(String transactionId, String cifCode) {
        log.info("transactionId: {} :: Start fetching statistical loan data for CIF code: {}", transactionId, cifCode);


        // Retrieve statistical loan data from the repository
        StatisticalLoanProjection projection = financialInfoRepository.getStatisticalLoan(cifCode);
        if (projection == null) {
            log.warn("transactionId: {} :: No statistical loan data found for CIF code: {}", transactionId, cifCode);
            throw new DataNotFoundException(MessageData.SERVER_ERROR);
        }
        FinancialInfo financialInfo = financialInfoRepository.findByCifCodeAndRequestStatusAndExpiredDateAfter(
                cifCode, RequestStatus.APPROVED, new Date(DateUtil.getCurrentTimeUTC7().getTime())
        );
        // Extract loan data from projection
        BigDecimal totalUnpaid = projection.getTotalUnpaidRepayment();
        BigDecimal totalPending = projection.getTotalPendingLoanAmount();
        BigDecimal totalPaid = projection.getTotalPaidRepayment();
        // Calculate total loan amount
        BigDecimal total = totalUnpaid.add(totalPending).add(totalPaid);
        log.info("transactionId: {} :: Loan statistics - Total Unpaid: {}, Total Pending: {}, Total Paid: {}, Total: {}",
                transactionId, totalUnpaid, totalPending, totalPaid, total);
        BigDecimal totalAmountRemain = financialInfo == null ? BigDecimal.ZERO : financialInfo.getLoanAmountMax()
                .subtract(totalUnpaid)
                .subtract(totalPending)
                .subtract(totalPaid);

        BigDecimal totalLoanAmountMax = financialInfo == null ? BigDecimal.ZERO : financialInfo.getLoanAmountMax();
        // Create pie chart data
        List<PieChartData> chartStatisticalLoanData = createPieChartData(totalUnpaid, totalPaid, totalPending, totalLoanAmountMax, totalAmountRemain);

        // Return response
        log.info("transactionId: {} :: Successfully retrieved statistical loan data for CIF code: {}", transactionId, cifCode);
        return DataResponseWrapper.builder()
                .data(chartStatisticalLoanData)
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message("Success")
                .build();
    }


    private static List<PieChartData> createPieChartData(BigDecimal unpaid, BigDecimal paid, BigDecimal pending, BigDecimal totalAmountMax, BigDecimal totalAmountRemain) {
        List<PieChartData> chartData = new ArrayList<>();
        if (totalAmountMax.compareTo(BigDecimal.ZERO) > 0) {
            if(unpaid.compareTo(BigDecimal.ZERO)>0)chartData.add(new PieChartData(LoanCategory.UNPAID_REPAYMENT.getLabel(), unpaid.divide(totalAmountMax, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)), Util.formatToVND(unpaid)));
            if(paid.compareTo(BigDecimal.ZERO)>0)chartData.add(new PieChartData(LoanCategory.PAID_REPAYMENT.getLabel(), paid.divide(totalAmountMax, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)), Util.formatToVND(paid)));
            if(pending.compareTo(BigDecimal.ZERO)>0)chartData.add(new PieChartData(LoanCategory.PENDING_LOAN.getLabel(), pending.divide(totalAmountMax, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)), Util.formatToVND(pending)));
            chartData.add(new PieChartData(LoanCategory.UN_LOAN.getLabel(), totalAmountRemain.divide(totalAmountMax, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)), Util.formatToVND(totalAmountRemain)));
        } else {
            chartData.add(new PieChartData(LoanCategory.UN_LOAN.getLabel(), BigDecimal.ZERO, Util.formatToVND(totalAmountRemain)));
        }
        return chartData;
    }


    @Override
    public DataResponseWrapper<Object> verifyFinancialInfo(String transactionId, String customerId) {
        Optional<FinancialInfo> financialInfoOptional = financialInfoRepository.findByIsDeletedAndCustomerIdAndIsExpiredFalseAndRequestStatus(false, customerId, RequestStatus.APPROVED);
        if (financialInfoOptional.isEmpty()) {
            log.info(MessageData.MESSAGE_LOG, transactionId, "Not found financial info of customer to verify");
            throw new DataNotFoundException(MessageData.FINANCIAL_INFO_NOT_FOUND);
        }
        // Validate if the financial information is approved
        if (!financialInfoOptional.get().getRequestStatus().equals(RequestStatus.APPROVED)) {
            log.info(MessageData.MESSAGE_LOG, MessageData.FINANCIAL_INFO_NOT_APPROVE.getMessageLog(), transactionId);
            throw new DataNotValidException(MessageData.FINANCIAL_INFO_NOT_APPROVE);
        }
        return DataResponseWrapper.builder()
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message("")
                .data(convertToFinancialInfoRp(financialInfoOptional.get(), null, false))
                .build();
    }

    private FinancialInfoRp convertToFinancialInfoRp(FinancialInfo financialInfo, CustomerDetailDTO customerDetailDTO, Boolean isDetail) {
        FinancialInfoRp financialInfoRp = new FinancialInfoRp();
        financialInfoRp.setCustomerId(financialInfo.getCustomerId());
        if (customerDetailDTO != null)
            financialInfoRp.setCustomerName(customerDetailDTO.getFullName());
        financialInfoRp.setFinancialInfoId(financialInfo.getId());
        financialInfoRp.setIncome(Util.formatToVND(new BigDecimal(financialInfo.getIncome())));
        financialInfoRp.setUnit(financialInfo.getUnit().toString());
        financialInfoRp.setCreditScore(financialInfo.getCreditScore().toString());
        financialInfoRp.setIncomeSource(financialInfo.getIncomeSource());
        financialInfoRp.setIncomeType(financialInfo.getIncomeType());
        financialInfoRp.setDebtStatus(financialInfo.getDebtStatus());
        List<LegalDocumentRp> legalDocumentsRpList = new ArrayList<>();
        if (!isDetail) {
            financialInfoRp.setCountLegalDocument(financialInfo.getFinancialInfoDocumentSet().size());
        } else {
            financialInfo.getFinancialInfoDocumentSet().forEach(financialInfoDocument -> {
                LegalDocuments legalDocuments = financialInfoDocument.getLegalDocuments();
                legalDocumentsRpList.add(convertToLegalDocumentRp(legalDocuments));
            });
            financialInfoRp.setLegalDocumentRpList(legalDocumentsRpList);
            financialInfoRp.setRequestStatus(financialInfo.getRequestStatus().toString());
            financialInfoRp.setExpiredDate(DateUtil.format(DateUtil.DD_MM_YYY_HH_MM_SLASH, financialInfo.getExpiredDate()));
            financialInfoRp.setNote(financialInfo.getNote());
        }
        financialInfoRp.setIsExpired(financialInfo.getIsExpired());
        financialInfoRp.setAmountLoanLimit(Util.formatToVND(financialInfo.getLoanAmountMax()));
        return financialInfoRp;
    }

    @Override
    public FinancialInfo getFinancialInfoByCustomerId(String customerId, String transactionId) {
        return financialInfoRepository.findByIsDeletedAndCustomerIdAndIsExpiredFalseAndRequestStatus(false, customerId, RequestStatus.APPROVED)
                .orElseThrow(() -> {
                    log.info(MessageData.MESSAGE_LOG_NOT_FOUND_DATA, transactionId, "Not found financial info with customer ", customerId);
                    return new DataNotFoundException(MessageData.FINANCIAL_INFO_NOT_FOUND);
                });
    }

    @Override
    public List<FinancialInfo> getListFinancialInfoByCifCode(String cifCode, String transactionId) {
        log.debug("TransactionId: {} - customer id - {} ", transactionId, cifCode);
        return financialInfoRepository.findAllByIsDeletedFalseAndCifCode(cifCode);
    }

    @Override
    public DataResponseWrapper<Object> getFinancialInfoByCifCode(String cifCode, String transactionId) {
        FinancialInfo financialInfo = financialInfoRepository.findByCifCodeAndIsDeletedFalseAndIsExpiredFalseAndRequestStatus(cifCode, RequestStatus.APPROVED).orElseThrow(
                () -> {
                    log.info(MessageData.MESSAGE_LOG, transactionId, String.format("Not found financial info with customer - cif code = %s ", cifCode));
                    return new DataNotFoundException(MessageData.FINANCIAL_INFO_NOT_FOUND);
                }
        );
        CustomerDetailDTO customerDetailDTO = customerDubboService.getCustomerByCifCode(cifCode);
        FinancialInfoRp financialInfoRp = convertToFinancialInfoRp(financialInfo, customerDetailDTO);
        return DataResponseWrapper.builder()
                .data(financialInfoRp)
                .message("")
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getDetailInfoActiveByCifCode(String cifCode, String transactionId) {
        List<FinancialInfo> listFinancialInfo = financialInfoRepository
                .findAllByRequestStatusInAndIsDeletedFalseAndCifCodeAndIsExpiredFalse(List.of(RequestStatus.APPROVED, RequestStatus.PENDING), cifCode);
        CustomerDetailDTO customerDetailDTO = customerDubboService.getCustomerByCifCode(cifCode);
        AccountInfoDTO accountInfoDTO = accountDubboService.getBankingAccount(cifCode);
        return DataResponseWrapper.builder()
                .data(convertToFinancialDetailRp(accountInfoDTO, customerDetailDTO, listFinancialInfo))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message("")
                .build();
    }

    private LegalDocumentRp convertToLegalDocumentRp(LegalDocuments legalDocuments) {
        LegalDocumentRp legalDocumentRp = new LegalDocumentRp();
        legalDocumentRp.setLegalDocumentId(legalDocuments.getId());
        legalDocumentRp.setDescription(legalDocuments.getDescription());
        legalDocumentRp.setImageBase64(legalDocuments.getUrlDocument());
        legalDocumentRp.setDocumentType(legalDocuments.getDocumentType().toString());
        if (legalDocuments.getDocumentGroupId() != null)
            legalDocumentRp.setDocumentGroupId(legalDocuments.getDocumentGroupId().getId());
        legalDocumentRp.setRequestStatus(legalDocuments.getRequestStatus().toString());
        legalDocumentRp.setExpirationDate(DateUtil.format(DateUtil.DD_MM_YYY_HH_MM_SLASH, legalDocuments.getExpirationDate()));
        return legalDocumentRp;
    }

    private FinancialInfoRp convertToFinancialInfoRp(FinancialInfo financialInfo, CustomerDetailDTO customerDetailDTO) {
        FinancialInfoRp financialInfoRp = new FinancialInfoRp();
        financialInfoRp.setCustomerId(financialInfo.getCustomerId());
        financialInfoRp.setCustomerName(customerDetailDTO.getFullName());
        financialInfoRp.setDateOfBirth(DateUtil.format(DateUtil.DD_MM_YYYY_SLASH, customerDetailDTO.getDob().atStartOfDay()));
        financialInfoRp.setIdentificationNumber(customerDetailDTO.getIdentityCard());
        financialInfoRp.setNumberPhone(customerDetailDTO.getPhone());
        BigDecimal amountLoanRemainLimit = financialInfo.getLoanAmountMax().subtract(financialInfoRepository.getLoanAmountRemainingLimit(customerDetailDTO.getCifCode()));
        financialInfoRp.setAmountLoanLimit(Util.formatToVND(amountLoanRemainLimit));
        return financialInfoRp;
    }

    private FinancialDetailRp convertToFinancialDetailRp(
            AccountInfoDTO bankingAccount,
            CustomerDetailDTO customerDetailDTO,
            List<FinancialInfo> financialInfoList) {
        FinancialDetailRp financialDetailRp = new FinancialDetailRp();
        financialDetailRp.setCustomerId(customerDetailDTO.getCustomerId());
        financialDetailRp.setCustomerName(customerDetailDTO.getFullName());
        financialDetailRp.setNumberPhone(customerDetailDTO.getPhone());
        financialDetailRp.setIdentificationNumber(customerDetailDTO.getIdentityCard());
        financialDetailRp.setDateOfBirth(DateUtil.format(DateUtil.DD_MM_YYYY_SLASH, customerDetailDTO.getDob()));
        financialDetailRp.setIsRegistered(financialInfoList.isEmpty());
        financialDetailRp.setAccountId(bankingAccount.getAccountId());
        financialDetailRp.setAccountType(bankingAccount.getAccountType().name());
        financialDetailRp.setAccountTypeDescription(bankingAccount.getAccountType().getDescription());
        if (!financialInfoList.isEmpty()) {
            FinancialInfo financialInfo = financialInfoList.get(0);
            financialDetailRp.setFinancialInfoId(financialInfo.getId());
            financialDetailRp.setRequestStatus(financialInfo.getRequestStatus().toString());
            financialDetailRp.setApplicableObjects(financialInfo.getApplicableObjects().name());
            if (financialInfo.getRequestStatus().equals(RequestStatus.APPROVED)) {
                LoanAmountInfoProjection loanAmountInfoProjection = loanDetailInfoRepository.getMaxLoanLimitAndCurrentLoanAmount(customerDetailDTO.getCustomerId()).orElse(null);
                if (loanAmountInfoProjection != null) {
                    financialDetailRp.setAmountLoanLimit(Util.formatToVND(financialInfo.getLoanAmountMax()));
                    BigDecimal amountMaybeLoanRemain = loanAmountInfoProjection.getLoanAmountMax().subtract(loanAmountInfoProjection.getTotalLoanedAmount());
                    financialDetailRp.setAmountMaybeLoanRemain(Util.formatToVND(amountMaybeLoanRemain));
                    financialDetailRp.setIsExpired(financialInfo.getIsExpired());
                    financialDetailRp.setExpiredDate(DateUtil.format(DateUtil.DD_MM_YYYY_SLASH, financialInfo.getExpiredDate()));
                }
            }
        }
        financialDetailRp.setBalanceBankingAccount(Util.formatToVND(bankingAccount.getCurrentAccountBalance()));
        financialDetailRp.setBankingAccountNumber(bankingAccount.getAccountNumber());
        return financialDetailRp;
    }
}

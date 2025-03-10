package org.demo.loanservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.common.*;
import org.demo.loanservice.controllers.exception.DataNotFoundException;
import org.demo.loanservice.dto.MapEntityToDto;
import org.demo.loanservice.dto.enumDto.ApplicableObjects;
import org.demo.loanservice.dto.enumDto.LoanType;
import org.demo.loanservice.dto.projection.LoanProductReportProjection;
import org.demo.loanservice.dto.request.LoanProductRq;
import org.demo.loanservice.dto.response.InterestRateRp;
import org.demo.loanservice.dto.response.LoanProductForUserRp;
import org.demo.loanservice.dto.response.LoanProductReportRp;
import org.demo.loanservice.dto.response.LoanProductRp;
import org.demo.loanservice.entities.InterestRate;
import org.demo.loanservice.entities.LoanProduct;
import org.demo.loanservice.repositories.LoanProductRepository;
import org.demo.loanservice.services.IInterestRateService;
import org.demo.loanservice.services.ILoanProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanProductServiceImpl implements ILoanProductService {
    private final LoanProductRepository loanProductRepository;
    private final IInterestRateService interestRateService;
    private final Logger log = LogManager.getLogger(LoanProductServiceImpl.class);

    @Override
    @Transactional
    public DataResponseWrapper<Object> save(LoanProductRq loanProductRq, String transactionId) {
        LoanProduct loanProduct = new LoanProduct();
        loanProduct.setLoanLimit(loanProductRq.getLoanLimit());
        loanProduct.setLoanCondition(loanProductRq.getLoanCondition().getBytes(StandardCharsets.UTF_8));
        loanProduct.setNameProduct(loanProductRq.getNameLoanProduct());
        loanProduct.setDescription(loanProductRq.getDescription().getBytes(StandardCharsets.UTF_8));
        loanProduct.setApplicableObjects(ApplicableObjects.valueOf(loanProductRq.getApplicableObjects()));
        loanProduct.setFormLoan(LoanType.valueOf(loanProductRq.getLoanForm()));
        loanProduct.setProductUrlImage("https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885_1280.jpg");//todo
        loanProduct.setUtilities(loanProductRq.getUtilities().getBytes(StandardCharsets.UTF_8));
        loanProduct.setIsDeleted(false);
        loanProduct.setTermLimit(loanProductRq.getTermLimit());
        loanProduct.setIsActive(false);
        loanProductRepository.save(loanProduct);
        return DataResponseWrapper.builder()
                .data(loanProduct.getId())
                .message("successfully")
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getById(String id, String transactionId) {
        LoanProduct loanProduct = getLoanProductById(id, transactionId);
        List<InterestRate> interestRateList = interestRateService.interestRateList(List.of(loanProduct.getId()));
        LoanProductRp loanProductRp = convertToLoanProductRp(loanProduct, interestRateList);
        return DataResponseWrapper.builder()
                .data(loanProductRp)
                .message("successfully")
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getAll(Integer pageNumber, Integer pageSize, String transactionId) {
        return null;
    }

    @Override
    public DataResponseWrapper<Object> active(String id, String transactionId) {
        LoanProduct loanProduct = getLoanProductById(id, transactionId);
        loanProduct.setIsActive(!loanProduct.getIsActive());
        loanProductRepository.save(loanProduct);
        return DataResponseWrapper.builder()
                .data(loanProduct.getId())
                .message("successfully")
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> update(String id, LoanProductRq loanProductRq, String transactionId) {
        return null;
    }

    @Override
    public DataResponseWrapper<Object> delete(String id, String transactionId) {
        LoanProduct loanProduct = getLoanProductById(id, transactionId);
        loanProduct.setIsDeleted(true);
        loanProductRepository.save(loanProduct);
        return DataResponseWrapper.builder()
                .data(loanProduct.getId())
                .message("successfully")
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> saveImageLoanProduct(String id, MultipartFile image, String transactionId) {
        return null;
    }

    @Override
    public LoanProduct getLoanProductById(String id, String transactionId) {
        Optional<LoanProduct> optionalLoanProduct = loanProductRepository.findByIdAndIsDeleted(id, false);
        if (optionalLoanProduct.isEmpty()) {
            log.info(MessageData.MESSAGE_LOG, MessageData.LOAN_PRODUCT_NOT_FOUNT.getMessageLog(), transactionId);
            throw new DataNotFoundException(MessageData.LOAN_PRODUCT_NOT_FOUNT);
        }
        return optionalLoanProduct.get();
    }

    @Override
    public DataResponseWrapper<Object> getAllByActive(Integer pageNumber, Integer pageSize, Boolean isActive, String transactionId) {
        // Create a Pageable object with sorting by createdDate in descending order
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());

        // Retrieve a paginated list of LoanProduct entities that are not deleted
        Page<LoanProduct> loanProductPage = loanProductRepository.findAllByIsDeletedFalseAndIsActive(isActive, pageable);
        List<LoanProduct> loanProductList = loanProductPage.getContent();
        log.info("Fetched LoanProduct list: {} records", loanProductList.size());

        // Extract the IDs of the retrieved LoanProduct entities
        Map<String, List<InterestRate>> interestRateMap = getInterestRateMap(loanProductList);

        // Prepare response data
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("totalRecords", loanProductPage.getTotalElements());

        // Convert LoanProduct entities to LoanProductRp response objects
        List<LoanProductRp> loanProductRpList = loanProductList
                .stream()
                .map(loanProduct -> {
                    List<InterestRate> interestRateListById = interestRateMap.getOrDefault(loanProduct.getId(), new ArrayList<>());
                    return convertToLoanProductRp(loanProduct, interestRateListById);
                }).toList();
        dataResponse.put("loanProductRpList", loanProductRpList);

        log.info("Successfully processed {} LoanProductRp records", loanProductRpList.size());

        // Return the wrapped response
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .message("Successfully retrieved loan products")
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getAllLoanProductIsActive(Integer pageNumber, Integer pageSize, String transactionId) {
        // Create a Pageable object with sorting by createdDate in descending order
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());

        // Retrieve a paginated list of LoanProduct entities that are not deleted
        Page<LoanProduct> loanProductPage = loanProductRepository.findAllByIsDeletedFalseAndIsActive(true, pageable);
        List<LoanProduct> loanProductList = loanProductPage.getContent();
        log.info("GetAllLoanProductIsActiveFetched:: LoanProduct list: {} records", loanProductList.size());

        Map<String, List<InterestRate>> interestRateMap = getInterestRateMap(loanProductList);

        // Prepare response data
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("totalRecords", loanProductPage.getTotalElements());

//         Convert LoanProduct entities to LoanProductForUserRp response objects
        List<LoanProductForUserRp> loanProductRpList = loanProductList
                .stream()
                .filter(loanProduct -> !interestRateMap.getOrDefault(loanProduct.getId(), List.of()).isEmpty())
                .map(loanProduct -> {
                    List<InterestRate> interestRateListById = interestRateMap.get(loanProduct.getId());
                    return convertToLoanProductForUserRp(loanProduct, interestRateListById);
                }).toList();
        dataResponse.put("loanProductForUserRpList", loanProductRpList);

        log.info("Successfully processed {} LoanProductForUserRp records", loanProductRpList.size());

        // Return the wrapped response
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .message("Successfully retrieved loan products")
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getLoanProductForUserById(String id, String transactionId) {
        LoanProduct loanProduct = getLoanProductById(id, transactionId);
        List<InterestRate> interestRateList = interestRateService.interestRateListByActiveTrue(List.of(loanProduct.getId()));
        LoanProductRp loanProductRp = convertToLoanProductRp(loanProduct, interestRateList);
        return DataResponseWrapper.builder()
                .data(loanProductRp)
                .message("successfully")
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .build();
    }

    @Override
    public DataResponseWrapper<Object> getLoanProductReport(String cifCode, Date fromDate, Date endDate, String transactionId) {
        LocalDate from=fromDate==null?null:fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end=endDate==null?null:endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<LoanProductReportProjection> loanProductReportProjection=loanProductRepository.getLoanProductReport(cifCode,from,end);
        return null;
    }

    private LoanProductForUserRp convertToLoanProductForUserRp(LoanProduct loanProduct, List<InterestRate> interestRateListById) {
        LoanProductForUserRp loanProductForUserRp = new LoanProductForUserRp();
        loanProductForUserRp.setLoanProductId(loanProduct.getId());
        loanProductForUserRp.setNameLoanProduct(loanProduct.getNameProduct());
        loanProductForUserRp.setMaxLoanTerm(loanProduct.getTermLimit());
        loanProductForUserRp.setMaxLoanAmount(Util.formatToVND(loanProduct.getLoanLimit()));
        loanProductForUserRp.setUrlImage(loanProduct.getProductUrlImage());
        double minInterestRate = interestRateListById.stream().map(InterestRate::getInterestRate).min(Double::compare).orElse(0.0);
        double maxInterestRate = interestRateListById.stream().map(InterestRate::getInterestRate).max(Double::compare).orElse(100.0);
        loanProductForUserRp.setMaxInterestRate(maxInterestRate);
        loanProductForUserRp.setMinInterestRate(minInterestRate);
        return loanProductForUserRp;
    }

    private Map<String, List<InterestRate>> getInterestRateMap(List<LoanProduct> loanProductList) {
        // Extract the IDs of the retrieved LoanProduct entities
        List<String> loanProductIdList = loanProductList.stream().map(LoanProduct::getId).toList();
        log.debug("LoanProduct ID list: {}", loanProductIdList);

        // Retrieve the list of InterestRate entities associated with the LoanProduct IDs
        List<InterestRate> interestRateList = interestRateService.interestRateListByActiveTrue(loanProductIdList);
        log.info("Fetched InterestRate list: {} records", interestRateList.size());

        // Group InterestRate entities by LoanProduct ID
        Map<String, List<InterestRate>> interestRateMap = interestRateList.stream()
                .collect(Collectors.groupingBy(ir -> ir.getLoanProduct().getId()));
        log.debug("InterestRate map created with {} entries", interestRateMap.size());
        return interestRateMap;
    }

    /**
     * Converts a LoanProduct entity to a LoanProductRp DTO.
     *
     * @param loanProduct      The LoanProduct entity to be converted.
     * @param interestRateList list containing lists of InterestRate entities of loan product
     * @return A LoanProductRp object representing the converted data.
     */
    public LoanProductRp convertToLoanProductRp(LoanProduct loanProduct, List<InterestRate> interestRateList) {
        LoanProductRp loanProductRp = new LoanProductRp();
        loanProductRp.setProductId(loanProduct.getId());
        loanProductRp.setProductName(loanProduct.getNameProduct());
        // Convert and set product description if it exists
        if (loanProduct.getDescription() != null) {
            loanProductRp.setProductDescription(new String(loanProduct.getDescription(), StandardCharsets.UTF_8));
        }
        loanProductRp.setApplicableObjects(loanProduct.getApplicableObjects().name());
        loanProductRp.setFormLoan(loanProduct.getFormLoan().name());
        loanProductRp.setProductUrlImage(loanProduct.getProductUrlImage());
        loanProductRp.setLoanLimit(Util.formatToVND(loanProduct.getLoanLimit()));
        loanProductRp.setIsActive(loanProduct.getIsActive());
        // Retrieve and convert interest rate data if the map is provided
        log.debug("interest rate list: {}", interestRateList);
        if (interestRateList != null && !interestRateList.isEmpty()) {
            log.debug("Set {} InterestRate records for LoanProduct ID: {}", interestRateList.size(), loanProduct.getId());
            // Convert InterestRate entities to DTOs
            List<InterestRateRp> interestRateRpList = interestRateList.stream()
                    .sorted(Comparator.comparing(InterestRate::getMinimumAmount))
                    .map(MapEntityToDto::convertToInterestRateRp)
                    .toList();
            loanProductRp.setInterestRate(interestRateRpList);
        } else {
            loanProductRp.setInterestRate(List.of());
        }
        loanProductRp.setTermLimit(loanProduct.getTermLimit());
        if (loanProduct.getUtilities() != null) {
            loanProductRp.setUtilities(new String(loanProduct.getUtilities(), StandardCharsets.UTF_8));
        }
        loanProductRp.setProductUrlImage(loanProduct.getProductUrlImage());
        if (loanProduct.getLoanCondition() != null) {
            loanProductRp.setLoanCondition(new String(loanProduct.getLoanCondition(), StandardCharsets.UTF_8));
        }
        // Format and set the created date as a string
        loanProductRp.setCreatedDate(DateUtil.format(DateUtil.YYYY_MM_DD_HH_MM_SS, loanProduct.getCreatedDate()));
        return loanProductRp;
    }
    private LoanProductReportRp mapObjectToLoanProductReportRp(LoanProductReportProjection loanProductReportProjection) {
        LoanProductReportRp loanProductReportRp = new LoanProductReportRp();
        loanProductReportRp.setLoanProductId(loanProductReportProjection.getId());
        loanProductReportRp.setTotalAmountLoanIsRepayment(loanProductReportProjection.getTotalAmountLoanIsRepayment());
        loanProductReportRp.setTotalAmountInterestIsPayment(loanProductReportProjection.getTotalAmountInterestIsPayment());
        return loanProductReportRp;
    }
}

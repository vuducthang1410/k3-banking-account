package org.demo.loanservice.services;

import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.dto.request.ApproveFinancialInfoRq;
import org.demo.loanservice.dto.request.FinancialInfoRq;
import org.demo.loanservice.entities.FinancialInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFinancialInfoService{
    DataResponseWrapper<Object> saveInfoIndividualCustomer(FinancialInfoRq financialInfoRq, List<MultipartFile> incomeVerificationDocuments, String transactionId);


    DataResponseWrapper<Object> getAllInfoIsByStatus(Integer pageNumber, Integer pageSize,String status, String transactionId);

    DataResponseWrapper<Object> getDetailInfoById(String id, String transactionId);

    DataResponseWrapper<Object> approveFinancialInfo(ApproveFinancialInfoRq financialInfoRq, String transactionId);


    DataResponseWrapper<Object> verifyFinancialInfo(String transactionId, String customerId);

    FinancialInfo getFinancialInfoByCustomerId(String id, String transactionId);

    List<FinancialInfo> getListFinancialInfoByCifCode(String cifCode, String transactionId);

    DataResponseWrapper<Object> getFinancialInfoByCifCode(String cifCode, String transactionId);
}

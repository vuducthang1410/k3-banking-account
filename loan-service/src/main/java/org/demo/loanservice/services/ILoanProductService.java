package org.demo.loanservice.services;

import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.dto.request.LoanProductRq;
import org.demo.loanservice.entities.LoanProduct;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

public interface ILoanProductService extends IBaseService<LoanProductRq> {
    DataResponseWrapper<Object> saveImageLoanProduct(String id, MultipartFile image,String transactionId);
    LoanProduct getLoanProductById(String id, String transactionId);

    DataResponseWrapper<Object> getAllByActive(Integer pageNumber, Integer pageSize, Boolean isActive, String transactionId);

    DataResponseWrapper<Object> getAllLoanProductIsActive(Integer pageNumber, Integer pageSize,  String transactionId);

    DataResponseWrapper<Object> getLoanProductForUserById(String id, String transactionId);

    DataResponseWrapper<Object> getLoanProductReport(String cifCode, Date fromDate, Date endDate, String transactionId);
}

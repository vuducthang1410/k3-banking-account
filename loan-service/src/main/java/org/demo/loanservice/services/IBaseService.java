package org.demo.loanservice.services;

import jakarta.validation.Valid;
import org.demo.loanservice.common.DataResponseWrapper;

public interface IBaseService <T>{

    DataResponseWrapper<Object> save(@Valid T t, String transactionId);

    DataResponseWrapper<Object> getById(String id, String transactionId);

    DataResponseWrapper<Object> getAll(Integer pageNumber, Integer pageSize, String transactionId);

    DataResponseWrapper<Object> active(String id, String transactionId);

    DataResponseWrapper<Object> update(String id,T t, String transactionId);

    DataResponseWrapper<Object> delete(String id, String transactionId);
}

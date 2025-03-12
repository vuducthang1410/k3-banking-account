package com.system.customer_service.service;

import com.system.common_library.enums.ObjectStatus;
import com.system.customer_service.dto.request.CustomerUpdateRequest;
import com.system.customer_service.dto.request.CustomerWorkflowRequest;
import com.system.customer_service.dto.response.CustomerResponse;

import java.util.List;
import java.util.Locale;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerWorkflowRequest customerWorkflowRequest, Locale locale);
    List<CustomerResponse> getCustomers(String name, String address);
    CustomerResponse getCustomer(String id);

    void deleteCustomer(String customerId);

    CustomerResponse updateCustomer(CustomerUpdateRequest request);
    CustomerResponse updateCustomerAd(String customerId, CustomerUpdateRequest request);

    CustomerResponse getMyInfo();
    CustomerResponse updateStatus(String customerId, ObjectStatus customerStatus);

    void createCustomerAndBankAccount(CustomerWorkflowRequest customerRequest, Locale locale);
    void createKycRequest(CustomerResponse customerResponse, CustomerWorkflowRequest customerRequest);

    void rollbackCustomerCreation(String customerId);
    void rollbackKycRequestCreation(String customerId);
}

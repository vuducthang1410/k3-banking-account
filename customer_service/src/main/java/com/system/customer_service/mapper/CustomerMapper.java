package com.system.customer_service.mapper;

import com.system.customer_service.dto.request.CustomerCreationRequest;
import com.system.customer_service.dto.response.CustomerResponse;
import com.system.customer_service.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toCustomer(CustomerCreationRequest request);

    @Mapping(target = "status", expression = "java(customer.getStatus().name())")
    CustomerResponse toCustomerResponse(Customer customer);

}

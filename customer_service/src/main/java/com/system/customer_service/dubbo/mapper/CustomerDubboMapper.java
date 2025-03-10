package com.system.customer_service.dubbo.mapper;

import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.ObjectStatus;
import com.system.customer_service.entity.Customer;
import org.springframework.stereotype.Service;

@Service
public class CustomerDubboMapper {
    public CustomerDetailDTO getCustomerDetail(Customer customer) {
        return CustomerDetailDTO.builder()
                .customerId(customer.getId())
                .cifCode(customer.getCifCode())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .dob(customer.getDob())
                .mail(customer.getMail())
                .fullName(customer.getLastName() + " " + customer.getFirstName())
                .lastName(customer.getLastName())
                .identityCard(customer.getIdentityCard())
                .gender(customer.getGender())
                .isActive(customer.getStatus() == ObjectStatus.ACTIVE)
                .status(customer.getStatus())
                .customerNumber(customer.getAccountNumber())
                .build();
    }

}

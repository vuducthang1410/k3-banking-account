package com.system.customer_service.mapper;

import com.system.customer_service.dto.response.KycResponse;
import com.system.customer_service.entity.Kyc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KycMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "kycStatus", target = "status")
    KycResponse fromKyc(Kyc kyc);
}

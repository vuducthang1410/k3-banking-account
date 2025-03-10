package com.system.customer_service.service;

import com.system.customer_service.dto.request.KycRequest;
import com.system.customer_service.dto.response.KycResponse;
import com.system.customer_service.enums.KycStatus;

public interface KycService {
    KycResponse getKycByCustomerId(String customerId);
    KycResponse updateKycStatus(String customerId, KycStatus kycStatus);

    void createKyc(KycRequest kycRequest) throws Exception;
    void deleteByCustomerId(String customerId);

}

package com.system.customer_service.controller;

import com.system.customer_service.dto.request.ApiResponse;
import com.system.customer_service.dto.response.KycResponse;
import com.system.customer_service.enums.KycStatus;
import com.system.customer_service.service.KycService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KycController {
    KycService kycService;

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ApiResponse<KycResponse> updateStatus(
            @RequestParam(name = "customerId") String customerId,
            @RequestParam(name = "status") KycStatus kycStatus
    ) {
        return ApiResponse.<KycResponse>builder()
                .result(kycService.updateKycStatus(customerId, kycStatus))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<KycResponse> getKyc(@RequestParam(name = "customerId") String customerId) {
        return ApiResponse.<KycResponse>builder()
                .result(kycService.getKycByCustomerId(customerId))
                .build();
    }
}

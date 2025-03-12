package com.system.customer_service.controller;

import com.system.common_library.enums.ObjectStatus;
import com.system.customer_service.dto.request.ApiResponse;
import com.system.customer_service.dto.request.CustomerRequest;
import com.system.customer_service.dto.request.CustomerUpdateRequest;
import com.system.customer_service.dto.request.CustomerWorkflowRequest;
import com.system.customer_service.dto.response.CustomerResponse;
import com.system.customer_service.service.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {
    CustomerService customerService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @ModelAttribute @Valid CustomerRequest customerRequest,
            Locale locale) throws IOException {

            log.info("Start create customer");

            CustomerWorkflowRequest customerWorkflowRequest = CustomerWorkflowRequest.builder()
                    .phone(customerRequest.getPhone())
                    .password(customerRequest.getPassword())
                    .firstName(customerRequest.getFirstname())
                    .lastName(customerRequest.getLastname())
                    .gender(customerRequest.getGender())
                    .email(customerRequest.getMail())
                    .address(customerRequest.getAddress())
                    .placeOrigin(customerRequest.getPlaceOrigin())
                    .identityCard(customerRequest.getIdentityCard())
                    .dob(customerRequest.getDob())
                    .identityCardFront(customerRequest.getIdentityCardFront().getBytes())
                    .identityCardBack(customerRequest.getIdentityCardBack().getBytes())
                    .avatar(customerRequest.getAvatar().getBytes())
                    .build();

            customerService.createCustomerAndBankAccount(customerWorkflowRequest, locale);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<CustomerResponse>builder()
                            .message("Success")
                            .build());
    }

    @GetMapping
    public ApiResponse<List<CustomerResponse>> getCustomers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String address) {

        List<CustomerResponse> customers = customerService.getCustomers(firstName, address);
        return ApiResponse.<List<CustomerResponse>>builder()
                .result(customers)
                .build();
    }

    @GetMapping("/{customerId}")
    ApiResponse<CustomerResponse> getCustomer(@PathVariable("customerId") String customerId) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.getCustomer(customerId))
                .build();
    }

    @DeleteMapping("/{customerId}")
    ApiResponse<String> deleteCustomer(@PathVariable String customerId) {
        customerService.deleteCustomer(customerId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping()
    ApiResponse<CustomerResponse> updateCustomer(@RequestBody CustomerUpdateRequest request) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.updateCustomer(request))
                .build();
    }

    @PutMapping("/admin/{customerId}")
    ApiResponse<CustomerResponse> updateCustomerAd(@PathVariable String customerId, @RequestBody CustomerUpdateRequest request) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.updateCustomerAd(customerId, request))
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<CustomerResponse> getMyInfo() {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.getMyInfo())
                .build();
    }

    @PutMapping("/status/{customerId}")
    public ApiResponse<CustomerResponse> changeStatus(@PathVariable String customerId, @RequestParam ObjectStatus status) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.updateStatus(customerId, status))
                .build();
    }
}

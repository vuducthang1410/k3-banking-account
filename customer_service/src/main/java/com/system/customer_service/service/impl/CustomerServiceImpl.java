package com.system.customer_service.service.impl;

import com.system.common_library.dto.account.CreateDubboBankingDTO;
import com.system.common_library.dto.request.customer.CreateCustomerCoreDTO;
import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.response.account.BranchInfoDTO;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.response.customer.CustomerExtraCoreDTO;
import com.system.common_library.enums.Gender;
import com.system.common_library.enums.ObjectStatus;
import com.system.common_library.service.AccountDubboService;
import com.system.customer_service.client.CustomerCoreFeignClient;
import com.system.customer_service.dto.identity.Credential;
import com.system.customer_service.dto.identity.TokenExchangeParam;
import com.system.customer_service.dto.identity.UserCreationParam;
import com.system.customer_service.dto.request.CustomerUpdateRequest;
import com.system.customer_service.dto.request.CustomerWorkflowRequest;
import com.system.customer_service.dto.request.KycRequest;
import com.system.customer_service.dto.response.CustomerResponse;
import com.system.customer_service.entity.Customer;
import com.system.customer_service.enums.TypeOTP;
import com.system.customer_service.exception.AppException;
import com.system.customer_service.exception.ErrorCode;
import com.system.customer_service.exception.ErrorNormalizer;
import com.system.customer_service.mapper.CustomerMapper;
import com.system.customer_service.repository.CustomerRepository;
import com.system.customer_service.repository.KycRepository;
import com.system.customer_service.service.*;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;
    KycRepository kycRepository;
    CustomerMapper customerMapper;
    KycService kycService;
    ProvinceService provinceService;
    IdentityClient identityClient;
    CustomerCoreFeignClient customerCoreFeignClient;
    ErrorNormalizer errorNormalizer;
    Keycloak keycloak;
    AuthenticationService authenticationService;

    @DubboReference
    AccountDubboService accountDubboService;

    @Value("${app.keycloak.realm}")
    @NonFinal
    String realm;

    @Value("${app.keycloak.admin.clientId}")
    @NonFinal
    String clientId;

    @Value("${app.keycloak.admin.clientSecret}")
    @NonFinal
    String clientSecret;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<CustomerResponse> getCustomers(String firstName, String address) {
        return customerRepository.findCustomers(firstName, address)
                .stream().map(customerMapper::toCustomerResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public CustomerResponse getCustomer(String id) {
        return customerMapper.toCustomerResponse(
                customerRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @Override
    public CustomerResponse getMyInfo() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        log.info("userId: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return customerMapper.toCustomerResponse(customer);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public CustomerResponse updateStatus(String customerId, ObjectStatus customerStatus) {
        var customer = this.customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        customer.setStatus(customerStatus);
        this.customerRepository.save(customer);

        return customerMapper.toCustomerResponse(customer);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCustomer(String customerId) {
        var customer = customerRepository.findCustomerById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        try {
            log.info("Gọi Core Banking để lấy thông tin customer với CIF: {}", customer.getCifCode());
            ResponseEntity<CustomerExtraCoreDTO> coreDTO = customerCoreFeignClient.getByCifCode(customer.getCifCode());

            if (coreDTO.getStatusCode() == HttpStatus.OK && coreDTO.getBody() != null) {
                String coreCustomerId = coreDTO.getBody().getId();
                log.info("Customer tồn tại trong Core Banking với ID: {}, tiến hành xoá...", coreCustomerId);
                customerCoreFeignClient.delete(coreCustomerId);
                log.info("Xoá customer từ Core Banking thành công.");
            } else {
                log.warn("Không tìm thấy customer trong Core Banking hoặc response không hợp lệ.");
            }
        } catch (FeignException fe) {
            log.error("Lỗi Feign khi gọi Core Banking: {}, Response: {}", fe.getMessage(), fe.contentUTF8(), fe);
        } catch (Exception e) {
            log.error("Lỗi khi gọi Core Banking: {}", e.getMessage(), e);
        }
        UsersResource usersResource = getUsersResource();
        usersResource.delete(customer.getUserId());
        kycRepository.deleteByCustomerId(customerId);
        customerRepository.deleteById(customerId);
    }

    @Override
    public CustomerResponse updateCustomer(CustomerUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        log.info("userId: {}", userId);

        // Tìm kiếm dưới database và trả về thông tin customer
        var customer = this.customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        mergerCustomer(customer, request, userId);

        //Lưu vào DB và trả về response
        customerRepository.save(customer);
        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    public CustomerResponse updateCustomerAd(String customerId, CustomerUpdateRequest request) {
        var customer = this.customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        mergerCustomer(customer, request, customer.getUserId());

        //Lưu vào DB và trả về response
        customerRepository.save(customer);
        return customerMapper.toCustomerResponse(customer);
    }

    public void mergerCustomer(Customer customer, CustomerUpdateRequest customerUpdateRequest, String userId) {
        var usersResource = getUsersResource();
        var userRepresentation = new UserRepresentation();

        // Nếu có trường nào cần update thì update còn không thì giữ nguyên
        if (!ObjectUtils.isEmpty(customerUpdateRequest.getFirstName())) {
            customer.setFirstName(customerUpdateRequest.getFirstName());
            userRepresentation.setFirstName(customerUpdateRequest.getFirstName());
        }
        if (!ObjectUtils.isEmpty(customerUpdateRequest.getLastName())) {
            customer.setLastName(customerUpdateRequest.getLastName());
            userRepresentation.setLastName(customerUpdateRequest.getLastName());
        }
        if (!ObjectUtils.isEmpty(customerUpdateRequest.getMail())) {
            customer.setMail(customerUpdateRequest.getMail());
            customer.setMailVerified(false);
            userRepresentation.setEmail(customerUpdateRequest.getMail());
            userRepresentation.setEmailVerified(false);
        }
        if (!ObjectUtils.isEmpty(customerUpdateRequest.getAddress())) {
            customer.setAddress(customerUpdateRequest.getAddress());
        }

        usersResource.get(userId).update(userRepresentation);
    }

    @Override
    public void createCustomerAndBankAccount(CustomerWorkflowRequest customerWorkflowRequest, Locale locale) {
        log.info("Bắt đầu quy trình tạo khách hàng và tài khoản ngân hàng cho số điện thoại: {}",
                customerWorkflowRequest.getPhone());

        //Tạo customer
        CustomerResponse customerResponse = createCustomer(customerWorkflowRequest, locale);

        // Tạo KYC
        createKycRequest(customerResponse, customerWorkflowRequest);

        //Tạo core customer
        createCoreCustomer(customerResponse);

        //Request tạo account
        createAccountDubbo(customerResponse);

        // Tạo mã xác thực và gửi mã qua mail cho khách hàng
        authenticationService.generateVerificationCode(customerResponse.getMail(), TypeOTP.MAIL);

    }

    @Transactional
    @Override
    public CustomerResponse createCustomer(CustomerWorkflowRequest customerWorkflowRequest, Locale locale) {
        String phone = customerWorkflowRequest.getPhone();
        String idCard = customerWorkflowRequest.getIdentityCard();
        String mail = customerWorkflowRequest.getEmail();

        if (customerRepository.existsByPhone(phone)) {
            log.error("Số điện thoại đã tồn tại: {}", phone);
            throw new AppException(ErrorCode.USER_PHONE_EXISTED, phone);
        }
        if (customerRepository.existsByIdentityCard(idCard)) {
            log.error("Số CMND/CCCD đã tồn tại: {}", idCard);
            throw new AppException(ErrorCode.USER_IDCARD_EXISTED, idCard);
        }
        if (customerRepository.existsByMail(mail)) {
            log.error("Email đã tồn tại: {}", mail);
            throw new AppException(ErrorCode.USER_MAIL_EXISTED, mail);
        }

        try {
            // Lấy token từ Client Keycloak
            var token = identityClient.exchangeToken(TokenExchangeParam.builder()
                    .grant_type("client_credentials")
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .scope("openid")
                    .build());

            log.info("TokenInfo {}", token);

            // Tạo user trong Keycloak
            var creationResponse = identityClient.createUser(
                    "Bearer " + token.getAccessToken(),
                    UserCreationParam.builder()
                            .username(customerWorkflowRequest.getPhone())
                            .firstName(customerWorkflowRequest.getFirstName())
                            .lastName(customerWorkflowRequest.getLastName())
                            .email(customerWorkflowRequest.getEmail())
                            .enabled(true)
                            .emailVerified(false)
                            .credentials(List.of(Credential.builder()
                                    .type("password")
                                    .temporary(false)
                                    .value(customerWorkflowRequest.getPassword())
                                    .build()))
                            .build());

            String userId = extractUserId(creationResponse);
            log.info("UserId {}", userId);

            Customer customer = Customer.builder()
                    .phone(phone)
                    .firstName(customerWorkflowRequest.getFirstName())
                    .lastName(customerWorkflowRequest.getLastName())
                    .gender(customerWorkflowRequest.getGender())
                    .mail(mail)
                    .address(customerWorkflowRequest.getAddress())
                    .identityCard(idCard)
                    .dob(customerWorkflowRequest.getDob())
                    .userId(userId)
                    .build();

            // Lưu customer vào DB
            customer = customerRepository.save(customer);

            return customerMapper.toCustomerResponse(customer);
        } catch (FeignException exception) {
            throw errorNormalizer.handleKeyCloakException(exception);
        }
    }

    @Transactional
    @Override
    public void createKycRequest(CustomerResponse customerResponse, CustomerWorkflowRequest customerRequest) {
        log.info("Bắt đầu tạo KYC khách hàng.");

        if (kycRepository.existsByIdentityNumber(customerResponse.getIdentityCard())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if(!provinceService.checkIdentityNumber(customerResponse.getIdentityCard(),
                customerRequest.getPlaceOrigin(),customerResponse.getDob(),customerResponse.getGender())) {
            throw new AppException(ErrorCode.IDENTITY_CARD_EXACT);
        }

        try {
            // Tạo KycRequest để truyền thông tin tạo KYC cho khách hàng (PENDING)
            KycRequest kycRequest = KycRequest.builder()
                    .customerId(customerResponse.getId())
                    .phone(customerResponse.getPhone())
                    .mail(customerResponse.getMail())
                    .identityNumber(customerResponse.getIdentityCard())
                    .gender(customerResponse.getGender())
                    .dob(customerResponse.getDob())
                    .placeOrigin(customerRequest.getPlaceOrigin())
                    .identityCardFront(customerRequest.getIdentityCardFront())
                    .identityCardBack(customerRequest.getIdentityCardBack())
                    .avatar(customerRequest.getAvatar())
                    .build();

            kycService.createKyc(kycRequest);
        } catch (Exception e) {
            log.error("Lỗi khi tạo KYC, rollback dữ liệu. Lỗi: {}", e.getMessage());
            // Rollback customer khi KYC lỗi
            rollbackCustomerCreation(customerResponse.getId());
            throw new RuntimeException("Tạo KYC thất bại, rollback dữ liệu!", e);
        }
    }

    @Transactional
    public void createCoreCustomer(CustomerResponse customerResponse) {
        log.info("Bắt đầu tạo core khách hàng.");

        CreateCustomerCoreDTO createData = CreateCustomerCoreDTO.builder()
                .fullName(customerResponse.getLastName() + " " + customerResponse.getFirstName())
                .gender(Arrays.stream(Gender.values())
                        .filter(g -> g.getDescription().equalsIgnoreCase(customerResponse.getGender()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid gender: " + customerResponse.getGender())))
                .address(customerResponse.getAddress())
                .phone(customerResponse.getPhone())
                .birthday(customerResponse.getDob())
                .email(customerResponse.getMail())
                .isActive(ObjectStatus.valueOf(customerResponse.getStatus()) == ObjectStatus.ACTIVE)
                .build();

        try {
            // Tạo customer trên core
            ResponseEntity<CustomerCoreDTO> resCoreCustomer = customerCoreFeignClient.create(createData);

            // Trả về cif code cho DB
            var customer = this.customerRepository.findByMail(createData.getEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            customer.setCifCode(resCoreCustomer.getBody().getCifCode());
            customerRepository.save(customer);
        } catch (Exception e) {
            log.error("Lỗi khi tạo core khách hàng: {}", e.getMessage());
            // Xóa khách hàng nếu lỗi
            rollbackKycRequestCreation(customerResponse.getId());
            rollbackCustomerCreation(customerResponse.getId());

            throw new RuntimeException("Tạo core khách hàng thất bại, rollback dữ liệu!", e);
        }
    }

    @Transactional
    public void createAccountDubbo(CustomerResponse customerResponse) {
        log.info("Bắt đầu tạo account khách hàng.");

        try {
            Customer customer = this.customerRepository.findCustomerById(customerResponse.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            BranchInfoDTO branch = accountDubboService.getRandomBranch();

            CreateDubboBankingDTO dto = CreateDubboBankingDTO.builder()
                    .customerId(customer.getId())
                    .cifCode(customer.getCifCode())
                    .phone(customer.getPhone())
                    .fullName(customer.getLastName() + " " + customer.getFirstName())
                    .email(customer.getMail())
                    .branchId(branch.getBranchId())
                    .build();

            AccountInfoDTO acc = accountDubboService.createBankingAccount(dto);

            customer.setAccountNumber(acc.getAccountNumber());
            customerRepository.save(customer);

            log.info("Tạo tài khoản thành công cho khách hàng ID: {}", customer.getId());
        } catch (Exception e) {
            log.error("Lỗi khi tạo core khách hàng: {}", e.getMessage(), e);

            // Thực hiện rollback dữ liệu
            rollbackKycRequestCreation(customerResponse.getId());
            rollbackCustomerCreation(customerResponse.getId());

            // Ném lỗi để Transactional rollback
            throw new RuntimeException("Tạo core khách hàng thất bại, rollback dữ liệu!", e);
        }
    }

    @Transactional
    @Override
    public void rollbackCustomerCreation(String customerId) {
        try {
            // Xóa khách hàng đã tạo
            var customer = customerRepository.findCustomerById(customerId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            log.info("Bắt đầu rollback tạo khách hàng với SĐT: {}", customer.getPhone());
            UsersResource usersResource = getUsersResource();
            usersResource.delete(customer.getUserId());
            deleteCustomer(customerId);
            log.info("Rollback thành công");
        } catch (Exception e) {
            throw new RuntimeException("Rollback tạo khách hàng thất bại", e);
        }
    }
    @Transactional
    @Override
    public void rollbackKycRequestCreation(String customerId) {
        try {
            log.info("Bắt đầu rollback KYC cho khách hàng");
            // Xóa kyc đã tạo
            kycService.deleteByCustomerId(customerId);
            log.info("Rollback thành công");
        } catch (Exception e) {
            throw new RuntimeException("Rollback Tạo KYC thất bại", e);
        }
    }

    private String extractUserId(ResponseEntity<?> response) {
        List<String> locationHeaders = response.getHeaders().get("Location");

        if (locationHeaders != null && !locationHeaders.isEmpty()) {
            String location = locationHeaders.get(0); // Lấy phần tử đầu tiên thay vì getFirst()
            String[] splitedStr = location.split("/");
            return splitedStr[splitedStr.length - 1];
        }

        throw new IllegalStateException("Không tìm thấy header 'Location' hoặc header bị trống");
    }

    public UsersResource getUsersResource(){
        return keycloak.realm(realm).users();
    }
}

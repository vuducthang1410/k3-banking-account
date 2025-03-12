package com.system.customer_service.dubbo;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.dto.user.UserDetailDTO;
import com.system.common_library.enums.Role;
import com.system.common_library.exception.DubboException;
import com.system.common_library.service.CustomerDubboService;
import com.system.customer_service.dubbo.mapper.CustomerDubboMapper;
import com.system.customer_service.entity.Customer;
import com.system.customer_service.exception.AppException;
import com.system.customer_service.exception.ErrorCode;
import com.system.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class CustomerDubboServiceImpl implements CustomerDubboService {

    private final CustomerRepository customerRepository;
    private final CustomerDubboMapper customerDubboMapper;

    @Override
    public UserDetailDTO loadUserByToken(String token) throws DubboException, ParseException {
        try {
            String userId = getUserIdFromToken(token);
            String roleName = getRoleFromToken(token);

            Customer customer = customerRepository.findByUserId(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            return UserDetailDTO.builder()
                    .id(customer.getId())
                    .username(customer.getPhone())
                    .password("")
                    .role(roleName)
                    .build();
        } catch (JOSEException e) {
            throw new RuntimeException("Lỗi xử lý token", e);
        }
    }

    @Override
    public List<CustomerDetailDTO> getCustomers(String firstName, String address) throws DubboException {
        List<Customer> customers;

        if (firstName != null && address != null) {
            customers = customerRepository.findByFirstNameContainingAndAddressContaining(firstName, address);
        } else if (firstName != null) {
            customers = customerRepository.findByFirstNameContaining(firstName);
        } else if (address != null) {
            customers = customerRepository.findByAddressContaining(address);
        } else {
            customers = customerRepository.findAll();
        }

        return customers.stream().map(customerDubboMapper::getCustomerDetail).toList();
    }

    @Override
    public CustomerDetailDTO getCustomerByCifCode(String cifCode) throws DubboException {
        Customer customer = customerRepository.findCustomerByCifCode(cifCode)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return customerDubboMapper.getCustomerDetail(customer);
    }

    @Override
    public List<CustomerDetailDTO> getListCustomerByCifCode(List<String> cifCode) {
        // Giả sử `customerRepository` có phương thức tìm kiếm theo `cifCode`
        List<Customer> customers = customerRepository.findByCifCodeIn(cifCode);

        // Chuyển đổi từ Customer sang CustomerDetailDTO
        return customers.stream()
                .map(customerDubboMapper::getCustomerDetail)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull CustomerDetailDTO getCustomerByCustomerId(String customerId) throws DubboException {
        Customer customer = customerRepository.findCustomerById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return customerDubboMapper.getCustomerDetail(customer);
    }

    @Override
    public List<CustomerDetailDTO> getReportCustomersByList(List<String> customerId) throws DubboException {
        // Giả sử `customerRepository` có phương thức tìm kiếm theo `cifCode`
        List<Customer> customers = customerRepository.findByIdIn(customerId);

        // Chuyển đổi từ Customer sang CustomerDetailDTO
        return customers.stream()
                .map(customerDubboMapper::getCustomerDetail)
                .collect(Collectors.toList());
    }

    private String getUserIdFromToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return signedJWT.getJWTClaimsSet().getSubject(); // Lấy "sub" từ token (User ID Keycloak)
    }

    private String getRoleFromToken(String token) throws ParseException, JOSEException {
        List<Role> priorityRoles = Arrays.asList(Role.ADMIN, Role.STAFF, Role.CUSTOMER);
        // Parse token
        JWTClaimsSet claims = SignedJWT.parse(token).getJWTClaimsSet();

        // Lấy realm_access dưới dạng Map<String, Object>
        Map<String, Object> realmAccess = (Map<String, Object>) claims.getClaim("realm_access");

        // Kiểm tra nếu realmAccess null hoặc không chứa "roles"
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return "DEFAULT_ROLE";
        }

        // Lấy danh sách roles từ realm_access
        List<String> roles = (List<String>) realmAccess.get("roles");

        // Kiểm tra role theo thứ tự ưu tiên
        for (Role role : priorityRoles) {
            if (roles.contains(role.name())) {
                return role.name();
            }
        }

        return "DEFAULT_ROLE";
    }
}

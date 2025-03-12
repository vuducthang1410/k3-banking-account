package com.system.customer_service.service.impl;

import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.ObjectStatus;
import com.system.common_library.service.NotificationDubboService;
import com.system.customer_service.dto.identity.AuthenticateParam;
import com.system.customer_service.dto.identity.IntrospectParam;
import com.system.customer_service.dto.identity.LogoutParam;
import com.system.customer_service.dto.identity.TokenExchangeResponse;
import com.system.customer_service.dto.request.*;
import com.system.customer_service.dto.response.IntrospectResponse;
import com.system.customer_service.dubbo.mapper.CustomerDubboMapper;
import com.system.customer_service.entity.Customer;
import com.system.customer_service.enums.TypeOTP;
import com.system.customer_service.exception.AppException;
import com.system.customer_service.exception.ErrorCode;
import com.system.customer_service.redis.CustomerCode;
import com.system.customer_service.redis.CustomerLoginAttempt;
import com.system.customer_service.repository.CustomerRepository;
import com.system.customer_service.service.AuthenticationService;
import com.system.customer_service.service.IdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    CustomerRepository customerRepository;
    CustomerCode customerCode;
    CustomerLoginAttempt customerLoginAttempt;
    Keycloak keycloak;
    IdentityClient identityClient;
    CustomerDubboMapper customerDubboMapper;

    @DubboReference
    NotificationDubboService notificationDubboService;

    @Value("${app.keycloak.admin.clientId}")
    @NonFinal
    private String clientId;

    @Value("${app.keycloak.admin.clientSecret}")
    @NonFinal
    String clientSecret;

    @Value("${app.keycloak.realm}")
    @NonFinal
    String realm;

    @Override
    public TokenExchangeResponse authenticate(AuthenticationRequest request) {
        var customer = customerRepository
                .findByPhone(request.getPhone())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        //Kiểm tra status customer
        ObjectStatus customerStatus = customer.getStatus();
        if(customerStatus.equals(ObjectStatus.SUSPENDED)) {
            throw new AppException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if(customerStatus.equals(ObjectStatus.CLOSED)) {
            throw new AppException(ErrorCode.ACCOUNT_CLOSED);
        }

        //Lấy số lần đăng nhập từ redis
        Integer attempts = customerLoginAttempt.getAttempt(request.getPhone());
        if (attempts == null) {
            attempts = 0;
        }

        //Kiểm tra số lần đăng nhập sai
        if(attempts >= 5) {
            customer.setStatus(ObjectStatus.SUSPENDED);
            customerRepository.save(customer);
            log.warn("Tài khoản đã bị khóa sau 5 lần đăng nhập sai.");
            throw new AppException(ErrorCode.ACCOUNT_LOCK);
        }

        // Gọi Keycloak để xác thực
        TokenExchangeResponse token;
        try {
            AuthenticateParam param = AuthenticateParam.builder()
                    .grant_type("password")
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .username(request.getPhone())
                    .password(request.getPassword())
                    .scope("openid")
                    .build();

            token = identityClient.authenticate(param);
            log.info("Token: {}", token);
        } catch (Exception e) {
            customerLoginAttempt.saveAttempt(request.getPhone(), ++attempts);
            log.info("số lần đăng nhập sai: {}", attempts);
            throw new AppException(ErrorCode.PASSWORD_ERROR);
        }

        //Reset số lần đăng nhập sai
        customerLoginAttempt.resetAttempt(request.getPhone());

        //Check customer đã xác thực mail chưa
        if(!customer.isMailVerified()) {
            //Tạo mã xác thực và gửi qua mail
            generateVerificationCode(customer.getMail(), TypeOTP.MAIL);
            throw new AppException(ErrorCode.LOGIN_VERIFY_MAIL);
        }

        return token;
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        IntrospectParam param = IntrospectParam.builder()
                .client_id(clientId)
                .client_secret(clientSecret)
                .token(request.getRefreshToken())
                .build();

        Map<String, Object> response = identityClient.introspect(param);
        log.info("Phản hồi từ máy chủ xác thực: {}", response);

        boolean isValid = response.get("active") != null && Boolean.TRUE.equals(response.get("active"));
        return new IntrospectResponse(isValid);
    }

    @Override
    public boolean logout(LogoutRequest request) {
        LogoutParam param = LogoutParam.builder()
                .client_id(clientId)
                .client_secret(clientSecret)
                .refresh_token(request.getToken())
                .build();

        Map<String, Object> response = identityClient.logout(param);
        return response != null && !response.containsKey("error");
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        log.info("Bắt đầu quá trình đổi mật khẩu cho email: {}", changePasswordRequest.getMail());
        // Tìm kiếm dưới database và trả về thông tin customer
        Customer customer = customerRepository.findByMail(changePasswordRequest.getMail())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        //Kiểm tra mã otp
        if (!changePasswordRequest.getOtp().equals(customerCode.getCode(changePasswordRequest.getMail()))) {
            throw new AppException(ErrorCode.ERROR_CODE);
        }

        // Kiểm tra xác nhận mật khẩu mới
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmationPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_CONFIRM);
        }

        // Xác thực mật khẩu cũ trước khi đổi
        try {
            AuthenticateParam param = AuthenticateParam.builder()
                    .grant_type("password")
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .username(customer.getPhone())
                    .password(changePasswordRequest.getCurrentPassword())
                    .scope("openid")
                    .build();

            identityClient.authenticate(param);
        } catch (Exception e) {
            throw new AppException(ErrorCode.PASSWORD_ERROR);
        }

        // Thay đổi mật khẩu trên Keycloak
        this.changeUserPassword(customer.getPhone(), changePasswordRequest.getNewPassword());
        log.info("Đổi mật khẩu thành công");
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String email = resetPasswordRequest.getMail();
        log.info("Bắt đầu reset mật khẩu cho email: {}", email);
        // Tìm kiếm dưới database và trả về thông tin customer
        Customer customer = customerRepository.findByMail(email)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        //Kiểm tra mã otp
        if (!resetPasswordRequest.getOtp().equals(customerCode.getCode(email))) {
            throw new AppException(ErrorCode.ERROR_CODE);
        }

        // Nếu mật khẩu mới và mật khẩu xác nhận không khớp thì hiển thị lỗi
        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmationPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_CONFIRM);
        }

        // Thay đổi mật khẩu
        this.changeUserPassword(customer.getPhone(), resetPasswordRequest.getNewPassword());
    }

    public void changeUserPassword(String phone, String newPassword) {
        List<UserRepresentation> users = keycloak.realm(realm).users().search(phone, true);

        if (users.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        UserRepresentation user = users.get(0);
        String userId = user.getId();
        log.info("Lấy được user {} từ Keycloak", user.getUsername());

        CredentialRepresentation newCredential = new CredentialRepresentation();
        newCredential.setTemporary(false);
        newCredential.setType(CredentialRepresentation.PASSWORD);
        newCredential.setValue(newPassword);

        keycloak.realm(realm)
                .users()
                .get(userId)
                .resetPassword(newCredential);
    }

    @Override
    public void verifyCustomerMail(String customerMail, Integer code, Locale locale) {
        log.info("Bắt đầu xác thực email: {}", customerMail);
        //Xác thực mail
        Customer customer = customerRepository.findByMail(customerMail)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        //Xác thực code
        Integer codeInRedis = customerCode.getCode(customerMail);
        log.info("Code OTP in redis: {}", codeInRedis);
        if(codeInRedis == null) {
            throw new AppException(ErrorCode.VERIFY_CODE_NOTFOUND);
        }
        if(!Objects.equals(code, codeInRedis)) {
            throw new AppException(ErrorCode.VERIFY_CODE_INVALID);
        }

        //Đổi status thành true trên Keycloak
        UserResource userResource = keycloak.realm(realm).users().get(customer.getUserId());
        UserRepresentation user = userResource.toRepresentation();
        user.setEmailVerified(true);
        userResource.update(user);

        // Đồng bộ với database
        customer.setMailVerified(true);
        customerRepository.save(customer);
        log.info("Xác thực email thành công.");

        //Thông báo cho người dùng đã xác thực thành công

    }

    @Override
    public boolean generateVerificationCode(String mail, TypeOTP otp) {
        log.info("Bắt đầu tạo mã OTP cho email: {}", mail);
        Customer customer = customerRepository.findByMail(mail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tạo OTP ngẫu nhiên (6 chữ số)
        int randomInt = 100000 + new Random().nextInt(900000);
        log.info("Mã OTP được tạo cho {}: {}", mail, randomInt);

        // Lưu OTP vào Redis (hết hạn sau 10 phút)
        customerCode.saveCode(mail, randomInt);

        CustomerDetailDTO customerDetail = customerDubboMapper.getCustomerDetail(customer);

        OTP otpNoti = OTP.builder()
                .otp(String.valueOf(randomInt))
                .expiredTime(LocalDateTime.now().plusMinutes(10))
                .build();

        if (otp.equals(TypeOTP.MAIL)) {
            return notificationDubboService.sendOtpCodeEmailCustomerRegistration(otpNoti, mail, customerDetail.getFullName());
        } else {
            return notificationDubboService.sendOTPCodeCustomerResetPassword(otpNoti, customerDetail);
        }
    }
}

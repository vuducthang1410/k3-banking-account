package com.system.customer_service.service;

import com.nimbusds.jose.JOSEException;
import com.system.customer_service.dto.identity.TokenExchangeResponse;
import com.system.customer_service.dto.request.*;
import com.system.customer_service.dto.response.IntrospectResponse;
import com.system.customer_service.enums.TypeOTP;

import java.text.ParseException;
import java.util.Locale;

public interface AuthenticationService {
    TokenExchangeResponse authenticate(AuthenticationRequest request);

    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    boolean logout(LogoutRequest request) throws ParseException, JOSEException;
    boolean generateVerificationCode(String mail, TypeOTP otp);
    void verifyCustomerMail(String customerMail, Integer code, Locale locale);
    void changePassword(ChangePasswordRequest changePasswordRequest);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}

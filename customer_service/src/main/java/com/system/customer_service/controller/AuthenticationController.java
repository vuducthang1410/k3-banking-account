package com.system.customer_service.controller;

import com.nimbusds.jose.JOSEException;
import com.system.customer_service.dto.identity.TokenExchangeResponse;
import com.system.customer_service.dto.request.*;
import com.system.customer_service.dto.response.IntrospectResponse;
import com.system.customer_service.enums.TypeOTP;
import com.system.customer_service.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @Operation(summary = "Token", description = "Tạo token mới")
    @PostMapping("/token")
    ApiResponse<TokenExchangeResponse> authenticate(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Thông tin đăng nhập",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AuthenticationRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "phone": "9661789389",
                          "password": "Hh@2222"
                        }
                    """)
            )
    ) @Valid AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<TokenExchangeResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyMail(
            @RequestParam(name = "mail") String mail,
            @RequestParam(name = "code") Integer code,
            Locale locale) {

            log.info("Verifying email: {}, code: {}", mail, code);

            // Gọi service để xác thực mã OTP
            authenticationService.verifyCustomerMail(mail, code, locale);

            // Trả về response thành công
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .message("Success")
                    .build();
            return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest)
    {
            // Gọi service để đổi mật khẩu
            authenticationService.changePassword(changePasswordRequest);

            // Trả về response thành công
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .message("Password changed successfully")
                    .build();
            return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Password reset successfully")
                .build());
    }

    @PostMapping("/generate-code")
    public ResponseEntity<ApiResponse<Integer>> generateVerificationCode(
            @RequestParam(name = "mail") String mail,
            @RequestParam(name = "type") TypeOTP typeOTP) {
            log.info("Generating verification code for user: {}", mail);

            // Tạo OTP
            authenticationService.generateVerificationCode(mail, typeOTP);

            // Trả về response thành công
            ApiResponse<Integer> response = ApiResponse.<Integer>builder()
                    .message("Success")
                    .build();
            return ResponseEntity.ok(response);
    }

}

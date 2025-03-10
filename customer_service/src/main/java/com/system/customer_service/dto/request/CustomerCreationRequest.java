package com.system.customer_service.dto.request;

import com.system.customer_service.validator.DobConstraint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerCreationRequest implements Serializable {

    @NotNull(message = "PASSWORD_MANDATORY")
    @NotEmpty(message = "PASSWORD_MANDATORY")
    @NotBlank(message = "PASSWORD_MANDATORY")
    @Size(min = 6, message = "INVALID_PASSWORD")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", message = "VALIDATION_PASSWORD_PATTERN")
    String password;

    @NotNull(message = "PHONE_MANDATORY")
    @NotEmpty(message = "PHONE_MANDATORY")
    @NotBlank(message = "PHONE_MANDATORY")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "VALIDATION_PHONE_INVALID")
    String phone;

    @NotNull(message = "ADDRESS_MANDATORY")
    @NotEmpty(message = "ADDRESS_MANDATORY")
    @NotBlank(message = "ADDRESS_MANDATORY")
    String address;

    @NotNull(message = "PLACE_ORIGIN_MANDATORY")
    @NotEmpty(message = "PLACE_ORIGIN_MANDATORY")
    String placeOrigin;

    @NotNull(message = "DOB_MANDATORY")
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;

    @NotBlank(message = "VALIDATION_EMAIL_NOT_BLANK")
    @Email(message = "VALIDATION_EMAIL_INVALID")
    String mail;

    @NotNull(message = "FIRSTNAME_MANDATORY")
    @NotEmpty(message = "FIRSTNAME_MANDATORY")
    @NotBlank(message = "FIRSTNAME_MANDATORY")
    String firstName;

    @NotNull(message = "LASTNAME_MANDATORY")
    @NotEmpty(message = "LASTNAME_MANDATORY")
    @NotBlank(message = "LASTNAME_MANDATORY")
    String lastName;

    @NotNull(message = "IDENTITY_CARD_MANDATORY")
    @NotEmpty(message = "IDENTITY_CARD_MANDATORY")
    @NotBlank(message = "IDENTITY_CARD_MANDATORY")
    @Size(min = 12, max = 12, message = "IDENTITY_CARD_EXACT")
    @Pattern(regexp = "\\d{12}", message = "IDENTITY_CARD_EXACT")
    String identityCard;

    @Pattern(regexp = "^(Nam|Nu)$", message = "GENDER_INVALID")
    @Schema(description = "Only 'Nam' or 'Nu'.", allowableValues = {"Nam", "Nu"})
    @NotNull(message = "GENDER_MANDATORY")
    @NotEmpty(message = "GENDER_MANDATORY")
    String gender;

    String avatar;

}

package com.system.customer_service.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequest implements Serializable {

    @NotNull(message = "{phone.mandatory}")
    @NotEmpty(message = "{phone.mandatory}")
    @NotBlank(message = "{phone.mandatory}")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "{validation.phone.invalid}")
    private String phone;

    @NotNull(message = "{password.mandatory}")
    @NotEmpty(message = "{password.mandatory}")
    @NotBlank(message = "{password.mandatory}")
    @Size(min = 6, message = "{validation.password.size}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", message = "{validation.password.pattern}")
    private String password;

    @NotNull(message = "{firstname.mandatory}")
    @NotEmpty(message = "{firstname.mandatory}")
    @NotBlank(message = "{firstname.mandatory}")
    private String firstname;

    @NotNull(message = "{lastname.mandatory}")
    @NotEmpty(message = "{lastname.mandatory}")
    @NotBlank(message = "{lastname.mandatory}")
    private String lastname;

    @Pattern(regexp = "^(Nam|Nu)$", message = "{gender.invalid}")
    @Schema(description = "Only 'Nam' or 'Nu'.", allowableValues = {"Nam", "Nu"})
    @NotNull(message = "{gender.mandatory}")
    @NotEmpty(message = "{gender.mandatory}")
    private String gender;

    @NotBlank(message = "{validation.email.notblank}")
    @Email(message = "{validation.email.invalid}")
    private String mail;

    @NotNull(message = "{address.mandatory}")
    @NotEmpty(message = "{address.mandatory}")
    @NotBlank(message = "{address.mandatory}")
    private String address;

    @NotNull(message = "{placeOrigin.mandatory}")
    @NotEmpty(message = "{placeOrigin.mandatory}")
    private String placeOrigin;

    @NotNull(message = "{identityCard.mandatory}")
    @NotEmpty(message = "{identityCard.mandatory}")
    @NotBlank(message = "{identityCard.mandatory}")
    @Size(min = 12, max = 12, message = "identityCard.exact")
    @Pattern(regexp = "\\d{12}", message = "identityCard.exact")
    private String identityCard;

    @NotNull(message = "{dob.mandatory}")
    private LocalDate dob;

    @JsonProperty("identityCardFront")
    private MultipartFile identityCardFront;

    @JsonProperty("identityCardBack")
    private MultipartFile identityCardBack;

    @JsonProperty("avatar")
    private MultipartFile avatar;

}

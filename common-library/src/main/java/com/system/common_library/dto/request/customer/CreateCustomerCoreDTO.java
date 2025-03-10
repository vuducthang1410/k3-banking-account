package com.system.common_library.dto.request.customer;

import com.system.common_library.enums.Gender;
import com.system.common_library.util.Constant;
import com.system.common_library.validation.annotation.BirthdayConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerCoreDTO implements Serializable {

    @NotNull(message = "{" + Constant.FULL_NAME_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.FULL_NAME_SIZE + "}")
    private String fullName;

    @NotNull(message = "{" + Constant.GENDER_REQUIRE + "}")
    private Gender gender;

    @Email(regexp = "^(.+)@(\\S+)$", message = "{" + Constant.INVALID_FORMAT_EMAIL + "}")
    private String email;

    @Pattern(regexp = "^(0\\d{9}|84\\d{9})$", message = "{" + Constant.INVALID_PHONE_NUMBER + "}")
    private String phone;

    private String address;

    @BirthdayConstraint
    @NotNull(message = "{" + Constant.BIRTHDAY_REQUIRE + "}")
    private LocalDate birthday;

    @NotNull(message = "{" + Constant.IS_ACTIVE_REQUIRE + "}")
    private Boolean isActive;

    private String description;
}

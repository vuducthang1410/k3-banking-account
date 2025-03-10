package com.system.common_library.dto.user;

import com.system.common_library.dto.report.config.FieldName;
import com.system.common_library.enums.ObjectStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerDetailDTO implements Serializable {
    String customerId;

    String cifCode;

    @FieldName("Số điện thoại")
    String phone;

    @FieldName("Địa chỉ")
    String address;

    @FieldName("Ngày sinh")
    LocalDate dob;

    @FieldName("Email")
    String mail;

    @FieldName("Họ và tên")
    String fullName;
    String firstName;
    String lastName;

    @FieldName("CMND/CCCD")
    String identityCard;

    @FieldName("Giới tính")
    String gender;

    boolean isActive;
    ObjectStatus status;

    String customerNumber;
}


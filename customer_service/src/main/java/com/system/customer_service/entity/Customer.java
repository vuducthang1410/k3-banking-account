package com.system.customer_service.entity;

import com.system.common_library.enums.ObjectStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Customer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "user_id")
    String userId;

    @Column(name = "cif_code")
    String cifCode;

    @Column(name = "account_number")
    String accountNumber;

    @Column(name = "phone", unique = true)
    String phone;

    @Column(name = "address")
    String address;

    @Column(name = "dob")
    LocalDate dob;

    @Column(name = "mail", unique = true)
    String mail;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    @Column(name = "identity_card", unique = true)
    String identityCard;

    @Column(name = "gender")
    String gender;

    @Column(name = "avatar")
    String avatar;

    @Enumerated(EnumType.STRING)
    ObjectStatus status;

    @Column(name = "mail_verified", columnDefinition = "boolean default false")
    boolean mailVerified;

    @PrePersist
    void setDefaultStatus() {
        if (status == null) {
            status = ObjectStatus.ACTIVE;
        }
    }

}

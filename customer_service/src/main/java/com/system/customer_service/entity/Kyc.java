package com.system.customer_service.entity;

import com.system.customer_service.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Kyc implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "customer_id",nullable = false)
    private Customer customer;

    @Column(name = "avatar")
    String avatar;

    @Column(name = "gender")
    String gender;

    @Column(name = "identity_card_front")
    String identityCardFront;

    @Column(name = "identity_card_back")
    String identityCardBack;

    @Column(name = "identity_number")
    String identityNumber;

    @Column(name = "kyc_status")
    @Enumerated(EnumType.STRING)
    KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "mail")
    String mail;

    @Column(name = "phone")
    String phone;

    @Column(name = "place_origin")
    String placeOrigin;

    @Column(name = "bad_debt")
    int badDebt;

}

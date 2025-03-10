package com.example.notification_service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "smsRegistration")
public class SMSRegistration {

    @Id
    private String customerCIF;

    @Column(name = "phone_number", nullable = false, length = 20)
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotNull
    private Boolean registered;

    @Column(name = "unregistered_at")
    private LocalDateTime unregisterAt;

}

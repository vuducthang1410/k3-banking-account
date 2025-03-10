package com.system.account_service.entities;

import com.system.account_service.entities.type.AccountStatus;
import com.system.account_service.entities.type.AccountTypes;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_common_info")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCommons extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String accountCommonId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String cifCode;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountTypes accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @PrePersist
    public void onCreate() {
        if(status == null) {
            status = AccountStatus.ACTIVE;
        }
    }
}

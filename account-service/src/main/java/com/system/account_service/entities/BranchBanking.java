package com.system.account_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branch_banking")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchBanking extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String branchId;

    @Column(nullable = false)
    private String branchName;

    @Column(nullable = false)
    private String address;

    private String description;
}

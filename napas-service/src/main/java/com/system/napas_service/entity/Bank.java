package com.system.napas_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Bank")
@Table(name = "tbl_bank",
        indexes = {
                @Index(name = "idx_name_code", columnList = "name, short_name, code")
        }
)
@EqualsAndHashCode(callSuper = true)
public class Bank extends BaseEntity implements Serializable {

    @Column(name = "name")
    private String name;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "code")
    private String code;

    @Column(name = "napas_code")
    private String napasCode;

    @Column(name = "swift_code")
    private String swiftCode;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "logo", length = 4000)
    private String logo;

    @Column(name = "logo_image_name")
    private String logoImageName;

    @OneToMany(mappedBy = "bank", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Account> accountList;
}

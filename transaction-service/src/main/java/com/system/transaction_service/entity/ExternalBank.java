package com.system.transaction_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ExternalBank")
@Table(name = "tbl_external_bank",
        indexes = {
                @Index(name = "idx_name_code", columnList = "name, short_name, code")
        }
)
@EqualsAndHashCode(callSuper = true)
public class ExternalBank extends BaseEntity implements Serializable {

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

    @LastModifiedDate
    @Column(name = "date_updated", insertable = false)
    private LocalDateTime dateUpdated;

    @CreatedBy
    @Column(name = "creator_id", nullable = false, updatable = false)
    private String creatorId;

    @LastModifiedBy
    @Column(name = "updater_id", insertable = false)
    private String updaterId;

    @Column(name = "state")
    private Boolean state;

    @OneToMany(mappedBy = "externalBank", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<ExternalTransaction> externalTransactionList;
}

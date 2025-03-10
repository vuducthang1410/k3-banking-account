package com.system.transaction_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ExternalTransaction")
@Table(name = "tbl_external_transaction")
@EqualsAndHashCode(callSuper = true)
public class ExternalTransaction extends Transaction implements Serializable {

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "external_bank_id")
    private ExternalBank externalBank;

    @Column(name = "napas_code")
    private String napasCode;

    @Column(name = "swift_code")
    private String swiftCode;
}

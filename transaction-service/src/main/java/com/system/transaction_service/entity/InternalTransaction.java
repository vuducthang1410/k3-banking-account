package com.system.transaction_service.entity;

import com.system.common_library.enums.Type;
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
@Entity(name = "InternalTransaction")
@Table(name = "tbl_internal_transaction")
@EqualsAndHashCode(callSuper = true)
public class InternalTransaction extends Transaction implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type;
}

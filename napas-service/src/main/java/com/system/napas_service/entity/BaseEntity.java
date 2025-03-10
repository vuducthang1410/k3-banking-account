package com.system.napas_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(value = AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Id
    @Column(name = "id", length = 26)
    private String id;

    @CreatedDate
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @LastModifiedDate
    @Column(name = "date_updated", insertable = false)
    private LocalDateTime dateUpdated;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "state")
    private Boolean state;

    @Column(name = "status")
    private Boolean status;
}

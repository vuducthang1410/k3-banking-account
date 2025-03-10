package org.demo.loanservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, length = 50, name = "ID")
    @Audited
    private String id;
//    @CreatedBy
//    @Column(updatable = false, nullable = false, length = 50, name = "CREATED_BY")
//    @Audited
//    private String createdBy;
//    @LastModifiedBy
//    @Column(length = 50, name = "LAST_MODIFIED_BY")
//    @Audited
//    private String lastModifiedBy;
    @CreatedDate
    @Column(updatable = false, nullable = false, length = 50, name = "CREATED_DATE")
    @Audited
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(length = 50, name = "LAST_MODIFIED_DATE")
    @Audited
    private LocalDateTime lastModifiedDate;
    @Column(length = 1, name = "IS_DELETED")
    @Audited
    @SQLRestriction("IS_DELETED = false")
    private Boolean isDeleted;

    @Version
    private Integer version;

    @PrePersist
    protected void onCreate() {
        this.isDeleted = false;
    }
}

package com.system.management.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.system.management.model.dto.Auditor;
import com.system.management.utils.AuditorConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @Column(name = "status")
    private String status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    @Convert(converter = AuditorConverter.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Auditor createdBy;

    @LastModifiedDate
    @Column(name = "modified_at")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date modifiedAt;

    @LastModifiedBy
    @Column(name = "modified_by")
    @Convert(converter = AuditorConverter.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Auditor modifiedBy;

    @Transient
    private String txtCreatedBy;

    @Transient
    private String txtModifiedBy;
}

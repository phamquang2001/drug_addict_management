package com.system.management.model.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assign_supports")
public class AssignSupport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "police_id")
    private Long policeId;

    @Column(name = "drug_addict_id")
    private Long drugAddictId;

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "ward_id")
    private Long wardId;

    @Column(name = "level")
    private Integer level;

    @Transient
    private Long permanentCityId;

    @Transient
    private Long permanentDistrictId;

    @Transient
    private Long permanentWardId;

    @Transient
    private String permanentAddressDetail;
}
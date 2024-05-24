package com.system.management.model.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drug_addicts")
public class DrugAddict extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "avatar")
    private byte[] avatar;

    @Column(name = "identify_number")
    private String identifyNumber;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "police_id")
    private Long policeId;

    @Column(name = "treatment_place_id")
    private Long treatmentPlaceId;

    @Column(name = "permanent_city_id")
    private Long permanentCityId;

    @Column(name = "permanent_district_id")
    private Long permanentDistrictId;

    @Column(name = "permanent_ward_id")
    private Long permanentWardId;

    @Column(name = "permanent_address_detail")
    private String permanentAddressDetail;

    @Column(name = "current_city_id")
    private Long currentCityId;

    @Column(name = "current_district_id")
    private Long currentDistrictId;

    @Column(name = "current_ward_id")
    private Long currentWardId;

    @Column(name = "current_address_detail")
    private String currentAddressDetail;

    @Column(name = "is_at_permanent")
    private Boolean isAtPermanent;
}
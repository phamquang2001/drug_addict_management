package com.system.management.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "drug_addicts")
public class DrugAddict extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_places_id")
    private TreatmentPlace treatmentPlace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permanent_city_id")
    private City permanentCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permanent_district_id")
    private District permanentDistrict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permanent_ward_id")
    private Ward permanentWard;

    @Column(name = "permanent_address_detail")
    private String permanentAddressDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_city_id")
    private City currentCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_district_id")
    private District currentDistrict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_ward_id")
    private Ward currentWard;

    @Column(name = "current_address_detail")
    private String currentAddressDetail;

    @Column(name = "is_at_permanent")
    private Boolean isAtPermanent;
}
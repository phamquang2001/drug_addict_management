package com.system.management.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "treatment_places")
public class TreatmentPlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "leader_full_name")
    private String leaderFullName;

    @Column(name = "leader_identify_number")
    private String leaderIdentifyNumber;

    @Column(name = "leader_phone_number")
    private String leaderPhoneNumber;

    @Column(name = "leader_email")
    private String leaderEmail;
}
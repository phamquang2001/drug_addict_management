package com.system.management.model.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "treatment_places")
public class TreatmentPlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "logo")
    private byte[] logo;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "ward_id")
    private Long wardId;

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
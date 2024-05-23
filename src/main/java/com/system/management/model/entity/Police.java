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
@Table(name = "polices")
public class Police extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "avatar")
    private byte[] avatar;

    @Column(name = "identify_number")
    private String identifyNumber;

    @Column(name = "password")
    private String password;

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

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "ward_id")
    private Long wardId;

    @Column(name = "level")
    private Integer level;

    @Column(name = "role")
    private Integer role;

    @Column(name = "assign_status")
    private Integer assignStatus;
}

package com.system.management.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugAddictDto extends BaseDto {

    private Long id;

    @JsonIgnore
    private byte[] avatar;

    private String strAvatar;

    private String identifyNumber;

    private String fullName;

    private Integer gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date dateOfBirth;

    private String phoneNumber;

    private String email;

    @JsonIgnore
    private Long policeId;

    private PoliceDto police;

    @JsonIgnore
    private Long treatmentPlaceId;

    private TreatmentPlaceDto treatmentPlace;

    @JsonIgnore
    private Long permanentCityId;

    private CityDto permanentCity;

    @JsonIgnore
    private Long permanentDistrictId;

    private DistrictDto permanentDistrict;

    @JsonIgnore
    private Long permanentWardId;

    private WardDto permanentWard;

    private String permanentAddressDetail;

    private String fullPermanent;

    @JsonIgnore
    private Long currentCityId;

    private CityDto currentCity;

    @JsonIgnore
    private Long currentDistrictId;

    private DistrictDto currentDistrict;

    @JsonIgnore
    private Long currentWardId;

    private WardDto currentWard;

    private String currentAddressDetail;

    private String fullCurrent;

    private Boolean isAtPermanent;
}
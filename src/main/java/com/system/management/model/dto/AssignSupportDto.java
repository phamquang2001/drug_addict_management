package com.system.management.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for {@link com.system.management.model.entity.AssignSupport}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignSupportDto extends BaseCadastralDto {

    private Long id;

    private Long policeId;

    private PoliceDto police;

    private Long drugAddictId;

    private DrugAddictDto drugAddict;

    private Integer level;

    private String levelName;

    @JsonIgnore
    private Long permanentCityId;

    @JsonIgnore
    private CityDto permanentCity;

    @JsonIgnore
    private Long permanentDistrictId;

    @JsonIgnore
    private DistrictDto permanentDistrict;

    @JsonIgnore
    private Long permanentWardId;

    @JsonIgnore
    private WardDto permanentWard;

    @JsonIgnore
    private String permanentAddressDetail;

    @JsonIgnore
    private String fullPermanent;
}
package com.system.management.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WardDto extends BaseDto {

    private Long id;

    private String code;

    private String fullName;

    @JsonIgnore
    private Long cityId;

    private CityDto city;

    @JsonIgnore
    private Long districtId;

    private DistrictDto district;
}

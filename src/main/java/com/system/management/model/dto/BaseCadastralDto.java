package com.system.management.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseCadastralDto extends BaseDto {

    @JsonIgnore
    private Long cityId;

    private CityDto city;

    @JsonIgnore
    private Long districtId;

    private DistrictDto district;

    @JsonIgnore
    private Long wardId;

    private WardDto ward;
}

package com.system.management.model.request.assign_support;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetListUnassignedDrugAddictRequest {

    @NotNull(message = "ID cảnh sát không được để trống")
    private Long policeId;

    private int page;

    private int size;

    private String identifyNumber;

    private String fullName;

    private Long cityId;

    private Long districtId;

    private Long wardId;
}

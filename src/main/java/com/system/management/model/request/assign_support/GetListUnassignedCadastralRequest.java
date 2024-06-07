package com.system.management.model.request.assign_support;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetListUnassignedCadastralRequest {

    @NotNull(message = "ID cảnh sát không được để trống")
    private Long policeId;

    private int page;

    private int size;

    private Long cityId;

    private Long districtId;

    private Long wardId;

    private Integer level;
}

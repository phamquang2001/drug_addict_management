package com.system.management.model.request.assign_support;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AssignDrugAddictRequest {

    @NotNull(message = "ID cảnh sát không được để trống")
    private Long policeId;

    @NotNull(message = "ID đối tượng không được để trống")
    private Long drugAddictId;
}

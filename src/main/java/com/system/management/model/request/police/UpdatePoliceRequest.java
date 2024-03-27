package com.system.management.model.request.police;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdatePoliceRequest extends InsertPoliceRequest {

    @NotNull(message = "ID cảnh sát không được để trống")
    private Long id;

    @NotNull(message = "Vai trò cảnh sát không được để trống")
    private Long role;
}

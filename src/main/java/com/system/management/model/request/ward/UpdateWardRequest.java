package com.system.management.model.request.ward;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateWardRequest extends InsertWardRequest {

    @NotNull(message = "ID phường/xã không được để trống")
    private Long id;
}

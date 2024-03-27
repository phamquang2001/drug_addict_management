package com.system.management.model.request.district;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateDistrictRequest extends InsertDistrictRequest {

    @NotNull(message = "ID quận/huyện không được để trống")
    private Long id;
}

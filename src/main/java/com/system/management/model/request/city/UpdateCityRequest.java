package com.system.management.model.request.city;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateCityRequest extends InsertCityRequest {

    @NotNull(message = "ID tỉnh/thành phố không được để trống")
    private Long id;
}

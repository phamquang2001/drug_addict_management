package com.system.management.model.request.city;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class InsertCityRequest {

    @NotBlank(message = "Mã tỉnh/thành phố không được để trống")
    private String code;

    @NotBlank(message = "Tên tỉnh/thành phố không được để trống")
    private String fullName;
}

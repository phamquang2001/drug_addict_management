package com.system.management.model.request.district;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class InsertDistrictRequest {

    @NotNull(message = "ID tỉnh/thành phố không được để rỗng")
    private Long cityId;

    @NotBlank(message = "Mã quận/huyện không được để trống")
    private String code;

    @NotBlank(message = "Tên quận/huyện không được để trống")
    private String fullName;
}

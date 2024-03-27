package com.system.management.model.request.ward;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class InsertWardRequest {

    @NotNull(message = "ID tỉnh/thành phố không được để rỗng")
    private Long cityId;

    @NotNull(message = "ID quận/huyện không được để rỗng")
    private Long districtId;

    @NotBlank(message = "Mã phường/xã không được để trống")
    private String code;

    @NotBlank(message = "Tên phường/xã không được để trống")
    private String fullName;
}

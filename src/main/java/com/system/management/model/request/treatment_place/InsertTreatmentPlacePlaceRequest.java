package com.system.management.model.request.treatment_place;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class InsertTreatmentPlacePlaceRequest {

    private String logo;

    @NotBlank(message = "Tên nơi cai nghiện không được để trống")
    private String fullName;

    @NotNull(message = "ID tỉnh/thành phố địa chỉ nơi cai nghiện không được để trống")
    private Long cityId;

    @NotNull(message = "ID quận/huyện địa chỉ nơi cai nghiện không được để trống")
    private Long districtId;

    private Long wardId;

    @NotBlank(message = "Địa chỉ chi tiết nơi cai nghiện không được để trống")
    private String addressDetail;

    @NotBlank(message = "Họ và tên người đứng đầu nơi cai nghiện không được để trống")
    private String leaderFullName;

    @NotBlank(message = "Số CCCD người đứng đầu nơi cai nghiện không được để trống")
    private String leaderIdentifyNumber;

    @NotBlank(message = "Số điện thoại người đứng đầu nơi cai nghiện không được để trống")
    private String leaderPhoneNumber;

    private String leaderEmail;
}

package com.system.management.model.request.drug_addict;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.system.management.utils.DateDeserializer;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class UpdateDrugAddictRequest {

    @NotNull(message = "ID đối tượng nghiện hút không được để trống")
    private Long id;

    private String avatar;

    @NotBlank(message = "Họ và tên không được để rỗng")
    private String fullName;

    @NotNull(message = "Giới tính không được để trống")
    private Integer gender;

    @JsonDeserialize(using = DateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private Date dateOfBirth;

    private String phoneNumber;

    private String email;

    private Long treatmentPlaceId;

    @NotNull(message = "ID tỉnh/thành phố nơi ở thường trú không được để trống")
    private Long permanentCityId;

    @NotNull(message = "ID quận/huyện nơi ở thường trú không được để trống")
    private Long permanentDistrictId;

    private Long permanentWardId;

    @NotNull(message = "Địa chỉ chi tiết nơi ở thường trú không được để trống")
    private String permanentAddressDetail;

    private Long currentCityId;

    private Long currentDistrictId;

    private Long currentWardId;

    private String currentAddressDetail;

    @NotNull(message = "Đối tượng có đang ở nơi ở hiện tại không được để trống")
    private Boolean isAtPermanent;
}

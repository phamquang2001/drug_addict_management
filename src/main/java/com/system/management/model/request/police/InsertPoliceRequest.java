package com.system.management.model.request.police;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.system.management.utils.DateDeserializer;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class InsertPoliceRequest {

    private String avatar;
    @NotBlank(message = "Số cccd không được để trống")
    private String identifyNumber;

    @NotBlank(message = "Họ và tên không được để rỗng")
    private String fullName;

    @NotNull(message = "Giới tính không được để trống")
    private Integer gender;

    @JsonDeserialize(using = DateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private Date dateOfBirth;

    private String phoneNumber;

    private String email;

    @NotNull(message = "Cấp bậc tài khoản không được để trống")
    private Integer level;

    private Long cityId;

    private Long districtId;

    private Long wardId;
}

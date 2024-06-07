package com.system.management.model.request.assign_support;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.system.management.utils.DateDeserializer;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class GetListAssignedDrugAddictRequest {

    @NotNull(message = "ID cảnh sát không được để trống")
    private Long policeId;

    private int page;

    private int size;

    private String identifyNumber;

    private String fullName;

    private Long cityId;

    private Long districtId;

    private Long wardId;

    @JsonDeserialize(using = DateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private Date startDate;

    @JsonDeserialize(using = DateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private Date endDate;
}

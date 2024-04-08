package com.system.management.model.request.drug_addict_request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.system.management.utils.DateDeserializer;
import lombok.Data;

import java.util.Date;

@Data
public class GetListDrugAddictRequestRequest {

    private int page;

    private int size;

    private String identifyNumber;

    private String fullName;

    @JsonDeserialize(using = DateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private Date startDate;

    @JsonDeserialize(using = DateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private Date endDate;

    private String status;

    private Long cityId;

    private Long districtId;

    private Long wardId;
}

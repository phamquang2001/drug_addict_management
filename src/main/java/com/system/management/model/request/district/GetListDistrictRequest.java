package com.system.management.model.request.district;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.system.management.utils.DateDeserializer;
import lombok.Data;

import java.util.Date;

@Data
public class GetListDistrictRequest {

    private int page;

    private int size;

    private Long cityId;

    private String code;

    private String fullName;

    private String status;
}

package com.system.management.model.request.police;

import lombok.Data;

@Data
public class GetListPoliceRequest {

    private int page;

    private int size;

    private String identifyNumber;

    private String fullName;

    private Integer role;

    private Integer level;

    private Long cityId;

    private Long districtId;

    private Long wardId;

    private Integer assignStatus;
}

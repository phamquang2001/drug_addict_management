package com.system.management.model.request.city;

import lombok.Data;

@Data
public class GetListCityRequest {

    private int page;

    private int size;

    private String code;

    private String fullName;

    private String status;
}

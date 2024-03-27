package com.system.management.model.request.treatment_place;

import lombok.Data;

@Data
public class GetListTreatmentPlaceRequest {

    private int page;

    private int size;

    private String fullName;

    private String leaderFullName;

    private String leaderPhoneNumber;

    private Long cityId;

    private Long districtId;

    private Long wardId;
}

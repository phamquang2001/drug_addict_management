package com.system.management.model.request.drug_addict;

import lombok.Data;

@Data
public class GetListDrugAddictRequest {

    private int page;

    private int size;

    private String identifyNumber;

    private String fullName;

    private Integer supervisorStatus;

    private Integer supervisorLevel;

    private String supervisorIdentifyNumber;

    private String supervisorFullName;

    private Long cityId;

    private Long districtId;

    private Long wardId;

    private Long treatmentPlaceId;

    private Long policeId;
}

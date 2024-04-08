package com.system.management.model.request.drug_addict_request;

import lombok.Data;

@Data
public class ConfirmDrugAddictRequestRequest {

    private Long id;

    private String status;

    private String reasonRejected;
}

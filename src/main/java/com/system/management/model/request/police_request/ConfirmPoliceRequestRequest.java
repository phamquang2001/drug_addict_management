package com.system.management.model.request.police_request;

import lombok.Data;

@Data
public class ConfirmPoliceRequestRequest {

    private Long id;

    private String status;

    private String reasonRejected;
}

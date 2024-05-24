package com.system.management.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreatmentPlaceDto extends BaseCadastralDto {

    private Long id;

    @JsonIgnore
    private byte[] logo;

    private String strLogo;

    private String fullName;

    private String addressDetail;

    private String leaderFullName;

    private String leaderIdentifyNumber;

    private String leaderPhoneNumber;

    private String leaderEmail;

    private String fullAddress;
}
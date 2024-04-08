package com.system.management.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PoliceRequestDto extends BaseCadastralDto {

    private Long id;

    @JsonIgnore
    private Long policeId;

    private PoliceDto police;

    @JsonIgnore
    private byte[] avatar;

    private String strAvatar;

    private String identifyNumber;

    private String fullName;

    private Integer gender;

    private Date dateOfBirth;

    private String phoneNumber;

    private String email;

    private Integer level;

    private String levelName;

    private Integer role;

    private String roleName;

    private String workPlace;

    private String reasonRejected;
}
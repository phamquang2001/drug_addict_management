package com.system.management.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PoliceDto extends BaseCadastralDto {

    private Long id;

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
}

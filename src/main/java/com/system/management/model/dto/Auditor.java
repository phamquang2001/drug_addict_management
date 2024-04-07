package com.system.management.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Auditor {

    private String identifyNumber;
    private String fullName;

    public Auditor(String identifyNumber) {
        this.identifyNumber = identifyNumber;
    }
}

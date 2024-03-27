package com.system.management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditor {

    private String identifyNumber;
    private String fullName;

    public Auditor(String identifyNumber) {
        this.identifyNumber = identifyNumber;
    }
}

package com.system.management.model.request.drug_addict;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateDrugAddictRequest extends InsertDrugAddictRequest {

    @NotNull(message = "ID đối tượng nghiện hút không được để trống")
    private Long id;
}

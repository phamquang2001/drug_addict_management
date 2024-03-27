package com.system.management.model.request.treatment_place;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateTreatmentPlacePlaceRequest extends InsertTreatmentPlacePlaceRequest {

    @NotNull(message = "ID nơi cai nghiện không được để trống")
    private Long id;
}

package com.system.management.controller;

import com.system.management.model.request.treatment_place.GetListTreatmentPlaceRequest;
import com.system.management.model.request.treatment_place.InsertTreatmentPlacePlaceRequest;
import com.system.management.model.request.treatment_place.UpdateTreatmentPlacePlaceRequest;
import com.system.management.service.TreatmentPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "**")
@RequestMapping(value = "/treatment_place", produces = MediaType.APPLICATION_JSON_VALUE)
public class TreatmentPlaceController {

    private final TreatmentPlaceService treatmentPlaceService;

    // Thêm mới
    @PostMapping(value = "/insert")
    public Object insert(@Valid @RequestBody InsertTreatmentPlacePlaceRequest request) {
        return treatmentPlaceService.insert(request);
    }

    // Cập nhật
    @PutMapping(value = "/update")
    public Object update(@Valid @RequestBody UpdateTreatmentPlacePlaceRequest request) {
        return treatmentPlaceService.update(request);
    }

    // Xóa
    @DeleteMapping("/delete")
    public Object delete(@RequestParam Long id) {
        return treatmentPlaceService.delete(id);
    }

    // Lấy danh sách
    @PostMapping(value = "/get-list")
    public Object getList(@Valid @RequestBody GetListTreatmentPlaceRequest request) {
        return treatmentPlaceService.getList(request);
    }

    // Xem chi tiết
    @GetMapping("/get")
    public Object get(@RequestParam Long id) {
        return treatmentPlaceService.get(id);
    }
}

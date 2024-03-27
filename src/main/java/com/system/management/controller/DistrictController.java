package com.system.management.controller;

import com.system.management.model.request.district.GetListDistrictRequest;
import com.system.management.model.request.district.InsertDistrictRequest;
import com.system.management.model.request.district.UpdateDistrictRequest;
import com.system.management.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/district", produces = MediaType.APPLICATION_JSON_VALUE)
public class DistrictController {

    private final DistrictService districtService;

    @PostMapping(value = "/insert")
    public Object login(@Valid @RequestBody InsertDistrictRequest request) {
        return districtService.insert(request);
    }

    @PutMapping(value = "/update")
    public Object login(@Valid @RequestBody UpdateDistrictRequest request) {
        return districtService.update(request);
    }

    @DeleteMapping("/delete")
    public Object delete(@RequestParam Long id) {
        return districtService.delete(id);
    }

    @PostMapping(value = "/get-list")
    public Object getList(@Valid @RequestBody GetListDistrictRequest request) {
        return districtService.getList(request);
    }
}

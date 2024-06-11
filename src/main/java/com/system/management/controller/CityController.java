package com.system.management.controller;

import com.system.management.model.request.city.GetListCityRequest;
import com.system.management.model.request.city.InsertCityRequest;
import com.system.management.model.request.city.UpdateCityRequest;
import com.system.management.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "**")
@RequestMapping(value = "/city", produces = MediaType.APPLICATION_JSON_VALUE)
public class CityController {

    private final CityService cityService;

    // Thêm mới
    @PostMapping(value = "/insert")
    public Object insert(@Valid @RequestBody InsertCityRequest request) {
        return cityService.insert(request);
    }

    // Cập nhật
    @PutMapping(value = "/update")
    public Object update(@Valid @RequestBody UpdateCityRequest request) {
        return cityService.update(request);
    }

    // Xóa
    @DeleteMapping("/delete")
    public Object delete(@RequestParam Long id) {
        return cityService.delete(id);
    }

    // Lấy danh sách
    @PostMapping(value = "/get-list")
    public Object getList(@Valid @RequestBody GetListCityRequest request) {
        return cityService.getList(request);
    }
}

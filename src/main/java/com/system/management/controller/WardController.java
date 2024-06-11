package com.system.management.controller;

import com.system.management.model.request.ward.GetListWardRequest;
import com.system.management.model.request.ward.InsertWardRequest;
import com.system.management.model.request.ward.UpdateWardRequest;
import com.system.management.service.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "**")
@RequestMapping(value = "/ward", produces = MediaType.APPLICATION_JSON_VALUE)
public class WardController {

    private final WardService wardService;

    // Thêm mới
    @PostMapping(value = "/insert")
    public Object login(@Valid @RequestBody InsertWardRequest request) {
        return wardService.insert(request);
    }

    // Cập nhật
    @PutMapping(value = "/update")
    public Object login(@Valid @RequestBody UpdateWardRequest request) {
        return wardService.update(request);
    }

    // Xóa
    @DeleteMapping("/delete")
    public Object delete(@RequestParam Long id) {
        return wardService.delete(id);
    }

    // Lấy danh sách
    @PostMapping(value = "/get-list")
    public Object getList(@Valid @RequestBody GetListWardRequest request) {
        return wardService.getList(request);
    }
}

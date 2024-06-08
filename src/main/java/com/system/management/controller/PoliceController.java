package com.system.management.controller;

import com.system.management.model.request.police.GetListPoliceRequest;
import com.system.management.model.request.police.InsertPoliceRequest;
import com.system.management.model.request.police.UpdatePoliceRequest;
import com.system.management.model.request.police_request.ConfirmPoliceRequestRequest;
import com.system.management.model.request.police_request.GetListPoliceRequestRequest;
import com.system.management.service.PoliceRequestService;
import com.system.management.service.PoliceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(value = "/police", produces = MediaType.APPLICATION_JSON_VALUE)
public class PoliceController {

    private final PoliceService policeService;

    private final PoliceRequestService policeRequestService;

    @PostMapping(value = "/insert")
    public Object insert(@Valid @RequestBody InsertPoliceRequest request) {
        return policeService.insert(request);
    }

    @PutMapping(value = "/update")
    public Object update(@Valid @RequestBody UpdatePoliceRequest request) {
        return policeService.update(request);
    }

    @DeleteMapping("/delete")
    public Object delete(@RequestParam Long id) {
        return policeService.delete(id);
    }

    @PostMapping(value = "/get-list")
    public Object getList(@Valid @RequestBody GetListPoliceRequest request) {
        return policeService.getList(request);
    }

    @GetMapping("/get")
    public Object get(@RequestParam Long id) {
        return policeService.get(id);
    }

    @PostMapping(value = "/get-list-request")
    public Object getListRequest(@Valid @RequestBody GetListPoliceRequestRequest request) {
        return policeRequestService.getListRequest(request);
    }

    @GetMapping("/get-request")
    public Object getRequest(@RequestParam Long id) {
        return policeRequestService.getRequest(id);
    }

    @PostMapping(value = "/confirm")
    public Object confirm(@Valid @RequestBody ConfirmPoliceRequestRequest request) {
        return policeRequestService.confirm(request);
    }
}

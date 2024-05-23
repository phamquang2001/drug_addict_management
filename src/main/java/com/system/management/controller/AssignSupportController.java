package com.system.management.controller;

import com.system.management.model.request.assign_support.AssignCadastralRequest;
import com.system.management.model.request.assign_support.AssignDrugAddictRequest;
import com.system.management.model.request.assign_support.GetListAssignCadastralRequest;
import com.system.management.model.request.assign_support.GetListAssignDrugAddictRequest;
import com.system.management.service.AssignSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(value = "/assign-support", produces = MediaType.APPLICATION_JSON_VALUE)
public class AssignSupportController {

    private final AssignSupportService assignSupportService;

    @GetMapping("/is-assigned")
    public Object isAssigned(@RequestParam Long id) {
        return assignSupportService.isAssigned(id);
    }

    @PostMapping(value = "/assign-drug-addict")
    public Object assignDrugAddict(@Valid @RequestBody AssignDrugAddictRequest request) {
        return assignSupportService.assignDrugAddict(request);
    }

    @PostMapping(value = "/get-list-drug-addict")
    public Object getList(@Valid @RequestBody GetListAssignDrugAddictRequest request) {
        return assignSupportService.getListAssignDrugAddict(request);
    }

    @PostMapping(value = "/assign-cadastral")
    public Object assignCadastral(@Valid @RequestBody AssignCadastralRequest request) {
        return assignSupportService.assignCadastral(request);
    }

    @PostMapping(value = "/get-list-cadastral")
    public Object getList(@Valid @RequestBody GetListAssignCadastralRequest request) {
        return assignSupportService.getListAssignCadastral(request);
    }

    @DeleteMapping("/delete")
    public Object delete(@RequestParam Long id) {
        return assignSupportService.delete(id);
    }

}

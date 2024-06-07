package com.system.management.controller;

import com.system.management.model.request.assign_support.*;
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

    @GetMapping("drug-addict/is-assigned")
    public Object isAssigned(@RequestParam Long id) {
        return assignSupportService.isAssigned(id);
    }

    @PostMapping(value = "drug-addict/assign")
    public Object assignDrugAddict(@Valid @RequestBody AssignDrugAddictRequest request) {
        return assignSupportService.assignDrugAddict(request);
    }

    @PostMapping(value = "drug-addict/get-list-assigned")
    public Object getListAssigned(@Valid @RequestBody GetListAssignedDrugAddictRequest request) {
        return assignSupportService.getListAssignedDrugAddict(request);
    }

    @PostMapping(value = "drug-addict/get-list-unassigned")
    public Object getListUnassigned(@Valid @RequestBody GetListUnassignedDrugAddictRequest request) {
        return assignSupportService.getListUnassignedDrugAddict(request);
    }

    @PostMapping(value = "cadastral/assign")
    public Object assignCadastral(@Valid @RequestBody AssignCadastralRequest request) {
        return assignSupportService.assignCadastral(request);
    }

    @PostMapping(value = "cadastral/get-list-assigned")
    public Object getListAssigned(@Valid @RequestBody GetListAssignCadastralRequest request) {
        return assignSupportService.getListAssignedCadastral(request);
    }

    @PostMapping(value = "cadastral/get-list-unassigned")
    public Object getListUnassigned(@Valid @RequestBody GetListUnassignedCadastralRequest request) {
        return assignSupportService.getListUnassignedCadastral(request);
    }

    @DeleteMapping("/delete")
    public Object delete(@RequestParam Long id) {
        return assignSupportService.delete(id);
    }

}

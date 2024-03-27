package com.system.management.controller;

import com.system.management.model.request.drug_addict.GetListDrugAddictRequest;
import com.system.management.model.request.drug_addict.InsertDrugAddictRequest;
import com.system.management.model.request.drug_addict.UpdateDrugAddictRequest;
import com.system.management.service.DrugAddictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/drug-addict", produces = MediaType.APPLICATION_JSON_VALUE)
public class DrugAddict {

    private final DrugAddictService drugAddictService;

    @PostMapping(value = "/insert")
    public Object insert(@Valid @RequestBody InsertDrugAddictRequest request) {
        return drugAddictService.insert(request);
    }

    @PutMapping(value = "/update")
    public Object login(@Valid @RequestBody UpdateDrugAddictRequest request) {
        return drugAddictService.update(request);
    }

    @DeleteMapping("/delete")
    public Object delete(@RequestParam Long id) {
        return drugAddictService.delete(id);
    }

    @PostMapping(value = "/get-list")
    public Object getList(@Valid @RequestBody GetListDrugAddictRequest request) {
        return drugAddictService.getList(request);
    }

    @GetMapping("/get")
    public Object get(@RequestParam Long id) {
        return drugAddictService.get(id);
    }
}

package com.system.management.controller;

import com.system.management.model.request.assign_support.*;
import com.system.management.service.AssignSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "**")
@RequestMapping(value = "/assign-support", produces = MediaType.APPLICATION_JSON_VALUE)
public class AssignSupportController {

    private final AssignSupportService assignSupportService;

    // Kiểm tra đối tượng đã được phân công cho cảnh sát nào hay chưa ?
    @GetMapping("drug-addict/is-assigned")
    public Object isAssigned(@RequestParam Long id) {
        return assignSupportService.isAssigned(id);
    }

    // Phân công đối tượng cho cảnh sát
    @PostMapping(value = "drug-addict/assign")
    public Object assignDrugAddict(@Valid @RequestBody AssignDrugAddictRequest request) {
        return assignSupportService.assignDrugAddict(request);
    }

    // Lấy danh sách đối tượng đã được phân công cho cảnh sát
    @PostMapping(value = "drug-addict/get-list-assigned")
    public Object getListAssigned(@Valid @RequestBody GetListAssignedDrugAddictRequest request) {
        return assignSupportService.getListAssignedDrugAddict(request);
    }

    // Lấy danh sách đối tượng chưa được phân công cho cảnh sát
    @PostMapping(value = "drug-addict/get-list-unassigned")
    public Object getListUnassigned(@Valid @RequestBody GetListUnassignedDrugAddictRequest request) {
        return assignSupportService.getListUnassignedDrugAddict(request);
    }

    // Phân công cảnh sát hỗ trợ địa chính
    @PostMapping(value = "cadastral/assign")
    public Object assignCadastral(@Valid @RequestBody AssignCadastralRequest request) {
        return assignSupportService.assignCadastral(request);
    }

    // Lấy danh sách địa chính cảnh sát đã được cử hỗ trợ
    @PostMapping(value = "cadastral/get-list-assigned")
    public Object getListAssigned(@Valid @RequestBody GetListAssignCadastralRequest request) {
        return assignSupportService.getListAssignedCadastral(request);
    }

    // Lấy danh sách địa chính cảnh sát chưa được cử hỗ trợ
    @PostMapping(value = "cadastral/get-list-unassigned")
    public Object getListUnassigned(@Valid @RequestBody GetListUnassignedCadastralRequest request) {
        return assignSupportService.getListUnassignedCadastral(request);
    }

    // Xóa phân công giám sát hoặc cử hỗ trợ
    @DeleteMapping("/delete")
    public Object delete(@RequestParam Long id) {
        return assignSupportService.delete(id);
    }

}

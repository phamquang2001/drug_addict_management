package com.system.management.controller;

import com.system.management.model.request.auth.ChangePasswordRequest;
import com.system.management.model.request.auth.LoginRequest;
import com.system.management.model.request.auth.UpdateAccountRequest;
import com.system.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/login")
    public Object login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping(value = "/verify")
    public Object verify(HttpServletRequest request) {
        return authService.verify(request.getHeader("Authorization"));
    }

    @GetMapping(value = "/refresh")
    public Object verifyToken(@RequestParam String token) {
        return authService.refresh(token);
    }

    @GetMapping(value = "/logout")
    public Object logout(HttpServletRequest request) {
        return authService.logout(request.getHeader("Authorization"));
    }

    @PostMapping(value = "/change-password")
    public Object changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(request);
    }

    @GetMapping(value = "/forget-password")
    public Object logout(@RequestParam String identifyNumber) {
        return authService.forgetPassword(identifyNumber);
    }

    @GetMapping(value = "/get-logged-account")
    public Object getLoggedAccount() {
        return authService.getAccountInfo();
    }

    @PostMapping(value = "/update-account")
    public Object updateAccount(@RequestBody UpdateAccountRequest request) {
        return authService.updateAccount(request);
    }
}

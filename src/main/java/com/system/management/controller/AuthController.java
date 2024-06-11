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

    // Đăng nhập
    @PostMapping(value = "/login")
    public Object login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Xác thực tài khoản
    @GetMapping(value = "/verify")
    public Object verify(HttpServletRequest request) {
        // Lấy ra access-token từ HEADER rồi truyền xuống function
        return authService.verify(request.getHeader("Authorization"));
    }

    // Đăng xuất
    @GetMapping(value = "/logout")
    public Object logout(HttpServletRequest request) {
        return authService.logout(request.getHeader("Authorization"));
    }

    // Đổi mật khẩu
    @PostMapping(value = "/change-password")
    public Object changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(request);
    }

    // Quên mật khẩu
    @GetMapping(value = "/forget-password")
    public Object logout(@RequestParam String identifyNumber) {
        return authService.forgetPassword(identifyNumber);
    }

    // Lấy ra thông tin tài khoản đang login
    @GetMapping(value = "/get-logged-account")
    public Object getLoggedAccount() {
        return authService.getAccountInfo();
    }

    // Cập nhật thông tin tài khoản đang login
    @PutMapping(value = "/update-account")
    public Object updateAccount(@Valid @RequestBody UpdateAccountRequest request) {
        return authService.updateAccount(request);
    }
}

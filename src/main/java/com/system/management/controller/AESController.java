package com.system.management.controller;

import com.system.management.utils.AESUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(value = "/aes", produces = MediaType.APPLICATION_JSON_VALUE)
public class AESController {

    private final AESUtils aesUtils;

    // Mã hóa AES
    @GetMapping(value = "/encrypt")
    public Object encrypt(@RequestParam String strToEncrypt) {
        return aesUtils.encrypt(strToEncrypt);
    }

    // Giải mã AES
    @GetMapping(value = "/decrypt")
    public Object decrypt(@RequestParam String strToDecrypt) {
        return aesUtils.decrypt(strToDecrypt);
    }
}

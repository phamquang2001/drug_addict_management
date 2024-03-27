package com.system.management.controller;

import com.system.management.utils.AESUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/aes", produces = MediaType.APPLICATION_JSON_VALUE)
public class AESController {

    private final AESUtils aesUtils;

    @GetMapping(value = "/encrypt")
    public Object encrypt(@RequestParam String strToEncrypt) {
        return aesUtils.encrypt(strToEncrypt);
    }

    @GetMapping(value = "/decrypt")
    public Object decrypt(@RequestParam String strToDecrypt) {
        return aesUtils.decrypt(strToDecrypt);
    }
}

package com.system.management.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Component("AESUtils")
public class AESUtils {

    private static final String AES = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5PADDING";

    @Value("${config.aes_key}")
    private String aesKey;

    private AESUtils() {
    }

    @SneakyThrows
    public String encrypt(String strToEncrypt) {
        SecretKeySpec secretKey = new SecretKeySpec(aesKey.getBytes(), AES);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);
    }

    @SneakyThrows
    public String decrypt(String strToDecrypt) {
        SecretKeySpec secretKey = new SecretKeySpec(aesKey.getBytes(), AES);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(strToDecrypt));

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}

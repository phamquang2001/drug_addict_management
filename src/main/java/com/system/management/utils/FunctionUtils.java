package com.system.management.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

/* Class chứa các function dùng chung xuyên suốt toàn bộ project */

@Component
public class FunctionUtils {

    private FunctionUtils() {
    }

    // Chuyển đổi từ thành không dấu và viết thường toàn bộ. Ví dụ: "Hà Nội" -> "ha noi"
    public static String normalizeAndLowercase(String input) {
        return Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    // Kiểm tra giá trị Long truyền xuống có null hoặc bằng 0
    public static boolean isNullOrZero(Long value) {
        return (value == null || value == 0L);
    }

    // Kiểm tra giá trị Integer truyền xuống có null hoặc bằng 0
    public static boolean isNullOrZero(Integer value) {
        return (value == null || value == 0);
    }

    // Xóa bỏ khoảng trắng đầu cuối và bị thừa ở giữa các thành phần trong từ. Ví dụ: " Hà    Nội " -> "Hà Nội"
    public static String capitalizeFully(String str) {
        String[] words = str.split("\\s+");
        StringBuilder output = new StringBuilder();

        for (String word : words) {
            output.append(StringUtils.capitalize(word.toLowerCase())).append(" ");
        }

        return output.toString().trim();
    }

    // Hàm sinh mật khẩu
    public static String generatePassword() {
        String specialCharacters = "!@#$%^&*()-_=+{}[]|:;<>,.?~";
        String specialCharacter =
                RandomStringUtils.random(2, specialCharacters.replace("/", "").replace("\\", ""));
        String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);

        return upperCaseLetters.concat(lowerCaseLetters).concat(numbers).concat(specialCharacter);
    }
}

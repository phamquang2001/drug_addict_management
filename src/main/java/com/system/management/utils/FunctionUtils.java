package com.system.management.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class FunctionUtils {

    private FunctionUtils() {
    }

    public static String normalizeAndLowercase(String input) {
        return Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static boolean isNullOrZero(Long value) {
        return (value == null || value == 0L);
    }

    public static boolean isNullOrZero(Integer value) {
        return (value == null || value == 0);
    }

    public static String capitalizeFully(String str) {
        String[] words = str.split("\\s+");
        StringBuilder output = new StringBuilder();

        for (String word : words) {
            output.append(StringUtils.capitalize(word.toLowerCase())).append(" ");
        }

        return output.toString().trim();
    }

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

package com.system.management.utils;

import com.system.management.model.dto.BaseCadastralDto;
import com.system.management.model.dto.PoliceDto;
import com.system.management.service.CityService;
import com.system.management.service.DistrictService;
import com.system.management.service.WardService;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
@RequiredArgsConstructor
public class FunctionUtils {

    private final CityService cityService;

    private final DistrictService districtService;

    private final WardService wardService;

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

    public static Object validateIdAndExistence(Long id, JpaRepository<?, Long> repository, String object) {
        if (id == null) {
            throw new BadRequestException("Mã " + object + " không được để trống!");
        }

        return repository.findById(id).orElseThrow(() -> new BadRequestException(object + " không tồn tại!"));
    }

    public void setCadastralInfo(BaseCadastralDto dto) {
        if (!FunctionUtils.isNullOrZero(dto.getCityId())) {
            dto.setCity(cityService.findByIdWithoutAuditor(dto.getCityId()));
        }

        if (!FunctionUtils.isNullOrZero(dto.getDistrictId())) {
            dto.setDistrict(districtService.findByIdWithoutAuditor(dto.getDistrictId()));
        }

        if (!FunctionUtils.isNullOrZero(dto.getWardId())) {
            dto.setWard(wardService.findByIdWithoutAuditor(dto.getWardId()));
        }
    }
}

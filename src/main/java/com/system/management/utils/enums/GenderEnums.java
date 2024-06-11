package com.system.management.utils.enums;

import java.util.Map;

/* Danh mục giới tính */

public enum GenderEnums {

    MALE(1, "Nam"),
    FEMALE(2, "Nữ"),
    ;

    public static final Map<Integer, GenderEnums> dict = Map.of(
            GenderEnums.MALE.value, GenderEnums.MALE,
            GenderEnums.FEMALE.value, GenderEnums.FEMALE
    );

    public final Integer value;

    public final String label;

    GenderEnums(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

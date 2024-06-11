package com.system.management.utils.enums;

import java.util.Map;

/* Danh mục cấp bậc (địa chính và tài khoản) */

public enum LevelEnums {

    CENTRAL(1, "Cấp trung ương"),
    CITY(2, "Cấp thành phố/tỉnh"),
    DISTRICT(3, "Cấp quận/huyện"),
    WARD(4, "Cấp phường/xã"),
    ;

    public static final Map<Integer, LevelEnums> dict = Map.of(
            LevelEnums.CENTRAL.value, LevelEnums.CENTRAL,
            LevelEnums.CITY.value, LevelEnums.CITY,
            LevelEnums.DISTRICT.value, LevelEnums.DISTRICT,
            LevelEnums.WARD.value, LevelEnums.WARD
    );

    public final Integer value;

    public final String label;

    LevelEnums(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

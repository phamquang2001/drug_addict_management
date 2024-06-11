package com.system.management.utils.enums;

import java.util.Map;

/* Danh mục vai trò */

public enum RoleEnums {

    POLICE(1, "Cảnh sát"),
    SHERIFF(2, "Cảnh sát trưởng"),
    ;

    public static final Map<Integer, RoleEnums> dict = Map.of(
            RoleEnums.POLICE.value, RoleEnums.POLICE,
            RoleEnums.SHERIFF.value, RoleEnums.SHERIFF
    );

    public final Integer value;

    public final String label;

    RoleEnums(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

package com.system.management.utils.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum AssignStatusEnums {

    UN_ASSIGN(0, "Chưa phân công"),
    ASSIGNED(1, "Đã phân công");

    public static final Map<Integer, AssignStatusEnums> dict = Map.of(
            AssignStatusEnums.UN_ASSIGN.value, AssignStatusEnums.UN_ASSIGN,
            AssignStatusEnums.ASSIGNED.value, AssignStatusEnums.ASSIGNED
    );

    public final Integer value;

    public final String label;

    AssignStatusEnums(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

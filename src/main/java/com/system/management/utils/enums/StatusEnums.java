package com.system.management.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnums {
  ACTIVE("Hoạt động"),
  DELETED("Xóa");
  private final String value;
}

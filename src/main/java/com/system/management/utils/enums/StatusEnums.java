package com.system.management.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/* Danh mục trạng thái */

@Getter
@AllArgsConstructor
public enum StatusEnums {

  ACTIVE("Hoạt động"),

  DELETED("Xóa"),

  WAIT("Chờ duyệt"),

  ACCEPT("Đồng ý"),

  REJECT("Từ chối");

  private final String value;
}

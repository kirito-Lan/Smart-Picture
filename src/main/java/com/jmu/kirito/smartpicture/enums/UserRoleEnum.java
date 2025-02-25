package com.jmu.kirito.smartpicture.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {

    USER("user", "普通用户", 1),
    ADMIN("admin", "管理员", 2);

    //角色
    private final String role;
    //角色描述
    private final String text;
    //角色等级
    private final int level;

    UserRoleEnum(String role, String text, int level) {
        this.role = role;
        this.text = text;
        this.level = level;
    }

    /**
     * 通过角色获取枚举
     *
     * @param role 角色
     * @return UserRoleEnum
     */
    public static UserRoleEnum getEnumByRole(String role) {
        for (UserRoleEnum value : values()) {
            if (value.getRole().equals(role)) {
                return value;
            }
        }
        return null;
    }

}

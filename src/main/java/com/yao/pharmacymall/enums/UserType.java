package com.yao.pharmacymall.enums;

import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
public enum UserType {

    CONSUMER(1, "消费者"),
    PHARMACY(2, "药店/商家"),
    HOSPITAL(3, "医院/诊所"),
    SUPPLIER(4, "供应商"),
    ADMIN(5, "平台管理员"),
    PHARMACIST(6, "执业药师");

    private final Integer code;
    private final String desc;

    UserType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserType getByCode(Integer code) {
        for (UserType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}

package com.yao.pharmacymall.enums;

import lombok.Getter;

/**
 * 商品类型枚举
 */
@Getter
public enum ProductType {

    OTC(1, "非处方药"),
    PRESCRIPTION(2, "处方药"),
    MEDICAL_DEVICE(3, "医疗器械"),
    HEALTH_PRODUCT(4, "保健品");

    private final Integer code;
    private final String desc;

    ProductType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProductType getByCode(Integer code) {
        for (ProductType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}

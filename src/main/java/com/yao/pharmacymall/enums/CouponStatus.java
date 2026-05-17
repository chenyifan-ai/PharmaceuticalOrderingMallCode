package com.yao.pharmacymall.enums;

import lombok.Getter;

@Getter
public enum CouponStatus {
    UNUSED(0, "未使用"),
    USED(1, "已使用"),
    EXPIRED(2, "已过期");

    private final Integer code;
    private final String desc;

    CouponStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CouponStatus fromCode(Integer code) {
        for (CouponStatus status : CouponStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
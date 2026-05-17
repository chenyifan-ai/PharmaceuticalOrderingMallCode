package com.yao.pharmacymall.enums;

import lombok.Getter;

/**
 * 处方审核状态枚举
 */
@Getter
public enum PrescriptionStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核拒绝"),
    EXPIRED(3, "已过期");

    private final Integer code;
    private final String desc;

    PrescriptionStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PrescriptionStatus getByCode(Integer code) {
        for (PrescriptionStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}

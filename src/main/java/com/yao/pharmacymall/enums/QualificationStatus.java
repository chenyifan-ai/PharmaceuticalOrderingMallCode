package com.yao.pharmacymall.enums;

import lombok.Getter;

@Getter
public enum QualificationStatus {
    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "未通过");

    private final Integer code;
    private final String desc;

    QualificationStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static QualificationStatus fromCode(Integer code) {
        for (QualificationStatus status : QualificationStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
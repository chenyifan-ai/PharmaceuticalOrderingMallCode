package com.yao.pharmacymall.enums;

import lombok.Getter;

/**
 * 发票状态枚举
 */
@Getter
public enum InvoiceStatus {

    PENDING(0, "待开票"),
    ISSUED(1, "已开票"),
    SENT(2, "已寄送"),
    CANCELLED(3, "已作废");

    private final Integer code;
    private final String desc;

    InvoiceStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "";
        }
        for (InvoiceStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "";
    }
}

package com.yao.pharmacymall.util;

import com.yao.pharmacymall.config.BusinessException;

/**
 * 解析前端 Map 请求体中的数值参数
 */
public final class RequestParamUtil {

    private RequestParamUtil() {
    }

    public static Long toLong(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException(fieldName + "不能为空");
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String s = ((String) value).trim();
            if (s.isEmpty()) {
                throw new BusinessException(fieldName + "不能为空");
            }
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new BusinessException(fieldName + "格式无效");
            }
        }
        throw new BusinessException(fieldName + "格式无效");
    }

    public static Integer toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static Long toLongOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String s = ((String) value).trim();
            if (s.isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

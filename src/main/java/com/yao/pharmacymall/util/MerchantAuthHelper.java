package com.yao.pharmacymall.util;

import com.yao.pharmacymall.config.BusinessException;

import javax.servlet.http.HttpServletRequest;

/**
 * 从请求上下文获取商家ID（由 AuthInterceptor 注入）
 */
public final class MerchantAuthHelper {

    private MerchantAuthHelper() {
    }

    public static Long requireMerchantId(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        if (merchantId == null) {
            throw new BusinessException("未找到商家信息，请先完成入驻审核");
        }
        return merchantId;
    }
}

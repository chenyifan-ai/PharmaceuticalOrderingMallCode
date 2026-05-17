package com.yao.pharmacymall.config;

import com.alibaba.fastjson2.JSON;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Merchant;
import com.yao.pharmacymall.enums.UserType;
import com.yao.pharmacymall.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 认证拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MerchantService merchantService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Token
        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            sendError(response, 401, "未登录或Token已过期");
            return false;
        }

        // 去除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            sendError(response, 401, "Token无效或已过期");
            return false;
        }

        // 将用户信息存入request
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("userType", userType);

        if (userId != null && userType != null
                && (UserType.SUPPLIER.getCode().equals(userType) || UserType.PHARMACY.getCode().equals(userType))) {
            Merchant merchant = merchantService.getMerchantByUserId(userId);
            if (merchant != null) {
                request.setAttribute("merchantId", merchant.getId());
            }
        }

        return true;
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, Integer code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code != null && code == 401
                ? HttpServletResponse.SC_UNAUTHORIZED
                : HttpServletResponse.SC_OK);
        PrintWriter writer = response.getWriter();
        writer.write(JSON.toJSONString(Result.error(code, message)));
        writer.flush();
        writer.close();
    }
}

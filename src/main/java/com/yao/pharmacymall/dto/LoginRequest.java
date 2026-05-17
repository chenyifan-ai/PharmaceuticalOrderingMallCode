package com.yao.pharmacymall.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 前端发送 { phone: xxx, password: xxx } 或 { username: xxx, password: xxx }
 * username/phone 可以是手机号或用户名
 */
@Data
public class LoginRequest {

    // 支持phone和username两个字段（兼容前端）
    @NotBlank(message = "用户名不能为空")
    private String phone;
    
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 获取实际的用户名（优先使用username，如果没有则使用phone）
     */
    public String getActualUsername() {
        return username != null ? username : phone;
    }
}

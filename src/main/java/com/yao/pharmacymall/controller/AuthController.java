package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.LoginRequest;
import com.yao.pharmacymall.dto.LoginResponse;
import com.yao.pharmacymall.dto.RegisterRequest;
import com.yao.pharmacymall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    /**
     * 发送短信验证码
     */
    @GetMapping("/sendCode")
    public Result<?> sendCode(@RequestParam String phone) {
        userService.sendSmsCode(phone);
        return Result.success("验证码已发送", null);
    }
}

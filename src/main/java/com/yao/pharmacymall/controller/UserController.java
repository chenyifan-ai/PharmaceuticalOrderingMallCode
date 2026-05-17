package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.User;
import com.yao.pharmacymall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(HttpServletRequest request, @RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUserInfo(userId, user);
        return Result.success("更新成功", null);
    }

    /**
     * 更新用户资料（C端采购方）
     */
    @PutMapping("/profile")
    public Result<?> updateUserProfile(HttpServletRequest request, @RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUserProfile(userId, user);
        return Result.success("更新成功", null);
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public Result<?> changePassword(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }

    /**
     * 重置密码（通过手机验证码）
     */
    @PostMapping("/resetPassword")
    public Result<?> resetPassword(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        userService.resetPassword(phone, code, newPassword);
        return Result.success("密码重置成功", null);
    }

    /**
     * 实名认证
     */
    @PostMapping("/realNameAuth")
    public Result<?> realNameAuth(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        String realName = params.get("realName");
        String idCard = params.get("idCard");
        userService.realNameAuth(userId, realName, idCard);
        return Result.success("认证提交成功", null);
    }
}
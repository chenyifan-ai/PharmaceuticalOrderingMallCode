package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.EnterpriseQualification;
import com.yao.pharmacymall.service.EnterpriseQualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 企业资质认证控制器
 */
@RestController
@RequestMapping("/api/qualification")
public class EnterpriseQualificationController {

    @Autowired
    private EnterpriseQualificationService enterpriseQualificationService;

    /**
     * 获取当前用户的企业资质信息
     */
    @GetMapping("/my")
    public Result<EnterpriseQualification> getMyQualification(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        EnterpriseQualification qualification = enterpriseQualificationService.getByUserId(userId);
        return Result.success(qualification);
    }

    /**
     * 提交企业资质认证
     */
    @PostMapping("/submit")
    public Result<Boolean> submitQualification(
            @RequestBody EnterpriseQualification qualification,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Boolean result = enterpriseQualificationService.submitQualification(userId, qualification);
        return Result.success(result);
    }

}
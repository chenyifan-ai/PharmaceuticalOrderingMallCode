package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.EnterpriseQualificationVO;
import com.yao.pharmacymall.entity.EnterpriseQualification;
import com.yao.pharmacymall.service.EnterpriseQualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员资质审核控制器
 */
@RestController
@RequestMapping("/api/admin/qualification")
public class AdminQualificationController {

    @Autowired
    private EnterpriseQualificationService enterpriseQualificationService;

    /**
     * 获取资质列表（管理员）
     */
    @GetMapping("/list")
    public Result<PageResult<EnterpriseQualification>> getQualificationList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        PageResult<EnterpriseQualification> result = enterpriseQualificationService.getQualificationList(status, page, pageSize);
        return Result.success(result);
    }

    /**
     * 审核资质（管理员）
     */
    @PostMapping("/review")
    public Result<Boolean> reviewQualification(
            @RequestParam Long qualificationId,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        
        Boolean result = enterpriseQualificationService.reviewQualification(qualificationId, status, reason, 1L);
        return Result.success(result);
    }

    /**
     * 获取资质详情（管理员）
     */
    @GetMapping("/{id}")
    public Result<EnterpriseQualificationVO> getQualificationDetail(@PathVariable Long id) {
        EnterpriseQualificationVO vo = enterpriseQualificationService.getDetailVo(id);
        return Result.success(vo);
    }
}

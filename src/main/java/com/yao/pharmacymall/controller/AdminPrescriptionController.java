package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Prescription;
import com.yao.pharmacymall.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员处方控制器
 */
@RestController
@RequestMapping("/api/admin/prescription")
public class AdminPrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    /**
     * 获取处方列表（管理员）
     */
    @GetMapping("/list")
    public Result<PageResult<Prescription>> getPrescriptionList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        PageResult<Prescription> result = prescriptionService.getAdminPrescriptionList(status, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取处方详情（管理员）
     */
    @GetMapping("/detail/{id}")
    public Result<Prescription> getPrescriptionDetail(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getAdminPrescriptionDetail(id);
        return Result.success(prescription);
    }
}

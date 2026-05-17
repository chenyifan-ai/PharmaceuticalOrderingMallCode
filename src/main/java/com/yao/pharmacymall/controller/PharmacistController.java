package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Prescription;
import com.yao.pharmacymall.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 药师工作台控制器(B端)
 */
@RestController
@RequestMapping("/api/pharmacist")
public class PharmacistController {

    @Autowired
    private PrescriptionService prescriptionService;

    /**
     * 获取待审核处方列表
     */
    @GetMapping("/prescriptions/pending")
    public Result<PageResult<Prescription>> getPendingPrescriptions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageResult<Prescription> result = prescriptionService.getPendingPrescriptions(page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取审核历史
     */
    @GetMapping("/prescriptions/history")
    public Result<PageResult<Prescription>> getAuditHistory(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long pharmacistId = (Long) request.getAttribute("userId");
        PageResult<Prescription> result = prescriptionService.getAuditHistory(pharmacistId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 审核处方
     */
    @PostMapping("/prescription/audit")
    public Result<?> auditPrescription(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long pharmacistId = (Long) request.getAttribute("userId");
        Long prescriptionId = ((Number) params.get("prescriptionId")).longValue();
        Integer status = (Integer) params.get("status");
        String remark = (String) params.get("remark");

        prescriptionService.auditPrescription(pharmacistId, prescriptionId, status, remark);
        return Result.success("审核完成", null);
    }

    /**
     * 获取处方详情（含图片信息）
     */
    @GetMapping("/prescription/{id}")
    public Result<Prescription> getPrescriptionDetail(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getById(id);
        return Result.success(prescription);
    }

    /**
     * 获取审核统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest request) {
        Long pharmacistId = (Long) request.getAttribute("userId");
        Map<String, Object> stats = prescriptionService.getPharmacistStatistics(pharmacistId);
        return Result.success(stats);
    }
}
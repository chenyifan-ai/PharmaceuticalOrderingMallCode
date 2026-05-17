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
 * 处方控制器(C端)
 */
@RestController
@RequestMapping("/api/prescription")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    /**
     * 上传处方
     */
    @PostMapping("/upload")
    public Result<Prescription> uploadPrescription(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long medicationUserId = ((Number) params.get("medicationUserId")).longValue();
        Integer prescriptionType = params.get("prescriptionType") != null
                ? ((Number) params.get("prescriptionType")).intValue()
                : 1;
        String imageUrl = (String) params.get("imageUrl");

        Prescription prescription = prescriptionService.uploadPrescription(userId, medicationUserId, prescriptionType, imageUrl);
        return Result.success(prescription);
    }

    /**
     * 获取用户处方列表
     */
    @GetMapping("/list")
    public Result<PageResult<Prescription>> getPrescriptionList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long userId = (Long) request.getAttribute("userId");
        PageResult<Prescription> result = prescriptionService.getUserPrescriptions(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取处方详情
     */
    @GetMapping("/detail/{id}")
    public Result<Prescription> getPrescriptionDetail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Prescription prescription = prescriptionService.getPrescriptionDetail(userId, id);
        return Result.success(prescription);
    }
}

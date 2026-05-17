package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.MerchantSettlementVO;
import com.yao.pharmacymall.entity.MerchantSettlement;
import com.yao.pharmacymall.service.MerchantSettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settlement")
public class AdminSettlementController {

    @Autowired
    private MerchantSettlementService merchantSettlementService;

    @GetMapping("/list")
    public Result<PageResult<MerchantSettlementVO>> list(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(merchantSettlementService.listVo(merchantId, status, page, pageSize));
    }

    @PostMapping("/generate")
    public Result<MerchantSettlement> generate(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        Long operatorId = (Long) request.getAttribute("userId");
        Long merchantId = Long.valueOf(body.get("merchantId").toString());
        LocalDate start = body.get("periodStart") != null
                ? LocalDate.parse(body.get("periodStart").toString()) : null;
        LocalDate end = body.get("periodEnd") != null
                ? LocalDate.parse(body.get("periodEnd").toString()) : null;
        return Result.success(merchantSettlementService.generate(merchantId, start, end, operatorId));
    }

    @PostMapping("/confirm/{id}")
    public Result<?> confirm(HttpServletRequest request, @PathVariable Long id) {
        Long operatorId = (Long) request.getAttribute("userId");
        merchantSettlementService.confirmSettle(id, operatorId);
        return Result.success("已确认结算");
    }
}

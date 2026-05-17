package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.PurchaseStatsVO;
import com.yao.pharmacymall.service.PurchaseStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/c/purchase")
public class CPurchaseStatsController {

    @Autowired
    private PurchaseStatsService purchaseStatsService;

    @GetMapping("/stats")
    public Result<PurchaseStatsVO> stats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(purchaseStatsService.getUserStats(userId));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        byte[] data = purchaseStatsService.exportCsv(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=purchase-stats.csv")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(data);
    }
}

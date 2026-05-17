package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Payment;
import com.yao.pharmacymall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/payment")
public class AdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/voucher/pending")
    public Result<PageResult<Payment>> pendingVouchers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(paymentService.listPendingVouchers(page, pageSize));
    }

    @PostMapping("/voucher/review/{id}")
    public Result<?> reviewVoucher(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String rejectReason = (String) body.get("rejectReason");
        paymentService.reviewTransferVoucher(id, approved, rejectReason);
        return Result.success(approved ? "已通过" : "已驳回");
    }
}

package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Payment;
import com.yao.pharmacymall.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 发起支付
     */
    @PostMapping("/create")
    public Result<Payment> createPayment(HttpServletRequest request, @RequestBody Map<String, Long> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long orderId = params.get("orderId");
        Integer paymentMethod = params.getOrDefault("paymentMethod", 1L).intValue();

        Payment payment = paymentService.createPayment(userId, orderId, paymentMethod);
        return Result.success(payment);
    }

    /**
     * 确认支付成功（前台支付页同步确认，更新订单状态）
     */
    @PostMapping("/voucher")
    public Result<?> submitVoucher(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long paymentId = params.get("paymentId") != null
                ? Long.valueOf(params.get("paymentId").toString()) : null;
        String voucherUrl = (String) params.get("voucherUrl");
        String transferRemark = (String) params.get("transferRemark");
        paymentService.submitTransferVoucher(userId, paymentId, voucherUrl, transferRemark);
        return Result.success("凭证已提交，请等待审核");
    }

    @PostMapping("/confirm")
    public Result<Payment> confirmPayment(HttpServletRequest request, @RequestBody Map<String, Long> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long paymentId = params.get("paymentId");
        if (paymentId == null) {
            return Result.error("paymentId 不能为空");
        }
        paymentService.confirmPayment(userId, paymentId);
        Payment payment = paymentService.getPaymentStatus(userId, paymentId);
        return Result.success(payment);
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{id}")
    public Result<Payment> getPaymentStatus(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Payment payment = paymentService.getPaymentStatus(userId, id);
        return Result.success(payment);
    }

    /**
     * 支付成功回调(第三方调用)
     * 注意: 此接口需要配置在第三方支付平台的回调地址中
     */
    @PostMapping("/callback/success")
    public Result<?> paymentSuccessCallback(@RequestBody Map<String, String> params) {
        log.info("收到支付成功回调请求: {}", params);

        try {
            // 1. 验证签名(防止伪造回调)
            String signature = params.remove("sign");
            if (!paymentService.verifyCallbackSignature(params, signature)) {
                log.error("支付回调签名验证失败");
                return Result.error("签名验证失败");
            }

            // 2. 提取关键参数
            String paymentNo = params.get("paymentNo");
            String transactionNo = params.get("transactionNo");
            String callbackData = params.toString();

            // 3. 处理支付回调
            paymentService.handlePaymentCallback(paymentNo, transactionNo, callbackData);

            log.info("支付成功回调处理完成");
            return Result.success("success");

        } catch (Exception e) {
            log.error("支付成功回调处理异常", e);
            return Result.error("回调处理失败: " + e.getMessage());
        }
    }

    /**
     * 支付失败回调(第三方调用)
     */
    @PostMapping("/callback/failure")
    public Result<?> paymentFailureCallback(@RequestBody Map<String, String> params) {
        log.info("收到支付失败回调请求: {}", params);

        try {
            String paymentNo = params.get("paymentNo");
            String failReason = params.get("failReason");

            paymentService.handlePaymentFailure(paymentNo, failReason);

            log.info("支付失败回调处理完成");
            return Result.success("success");

        } catch (Exception e) {
            log.error("支付失败回调处理异常", e);
            return Result.error("回调处理失败: " + e.getMessage());
        }
    }
}

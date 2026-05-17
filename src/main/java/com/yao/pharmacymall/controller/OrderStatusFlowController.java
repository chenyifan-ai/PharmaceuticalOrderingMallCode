package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.OrderStatusLog;
import com.yao.pharmacymall.service.OrderStatusFlowService;
import com.yao.pharmacymall.service.OrderStatusLogService;
import com.yao.pharmacymall.util.MerchantAuthHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 订单状态流转控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/order/status")
public class OrderStatusFlowController {

    @Autowired
    private OrderStatusFlowService orderStatusFlowService;

    @Autowired
    private OrderStatusLogService orderStatusLogService;

    /**
     * C端: 用户取消订单
     */
    @PostMapping("/cancel")
    public Result<?> cancelOrder(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = (String) params.get("reason");

        orderStatusFlowService.cancelOrderByUser(userId, orderId, reason);
        return Result.success("订单已取消");
    }

    /**
     * B端: 商家取消订单
     */
    @PostMapping("/merchant/cancel")
    public Result<?> merchantCancelOrder(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = (String) params.get("reason");

        orderStatusFlowService.cancelOrderByMerchant(merchantId, orderId, reason);
        return Result.success("订单已取消");
    }

    /**
     * B端: 商家发货
     */
    @PostMapping("/ship")
    public Result<?> shipOrder(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String logisticsCompany = (String) params.get("logisticsCompany");
        String logisticsNo = (String) params.get("logisticsNo");

        orderStatusFlowService.shipOrder(merchantId, orderId, logisticsCompany, logisticsNo);
        return Result.success("发货成功");
    }

    /**
     * C端: 用户确认收货
     */
    @PostMapping("/confirm")
    public Result<?> confirmReceive(HttpServletRequest request, @RequestBody Map<String, Long> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long orderId = params.get("orderId");

        orderStatusFlowService.confirmReceive(userId, orderId);
        return Result.success("确认收货成功");
    }

    /**
     * C端: 申请退款
     */
    @PostMapping("/refund/apply")
    public Result<?> applyRefund(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = (String) params.get("reason");

        orderStatusFlowService.applyRefund(userId, orderId, reason);
        return Result.success("退款申请已提交");
    }

    /**
     * B端: 处理退款申请
     */
    @PostMapping("/refund/process")
    public Result<?> processRefund(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Boolean approved = (Boolean) params.get("approved");
        String remark = (String) params.get("remark");

        orderStatusFlowService.processRefund(merchantId, orderId, approved, remark);
        return Result.success(approved ? "已同意退款" : "已拒绝退款");
    }

    /**
     * 获取订单状态变更历史
     */
    @GetMapping("/history/{orderId}")
    public Result<List<OrderStatusLog>> getStatusHistory(@PathVariable Long orderId) {
        List<OrderStatusLog> history = orderStatusLogService.getOrderStatusHistory(orderId);
        return Result.success(history);
    }
}

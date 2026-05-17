package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.service.OrderService;
import com.yao.pharmacymall.service.OrderStatusFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员订单控制器
 */
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStatusFlowService orderStatusFlowService;

    /**
     * 获取订单列表（管理员）
     */
    @GetMapping("/list")
    public Result<PageResult<Order>> getOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageResult<Order> result = orderService.getAdminOrderList(keyword, status, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取订单详情（管理员）
     */
    @GetMapping("/detail/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        Order order = orderService.getAdminOrderDetail(id);
        return Result.success(order);
    }

    /**
     * 管理员代发货
     */
    @PostMapping("/ship/{id}")
    public Result<?> shipOrder(@PathVariable Long id, @RequestBody java.util.Map<String, String> params) {
        orderService.adminShipOrder(id, params.get("logisticsCompany"), params.get("logisticsNo"));
        return Result.success("发货成功", null);
    }

    /**
     * 管理员取消订单
     */
    @PostMapping("/cancel/{id}")
    public Result<?> cancelOrder(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, String> params) {
        String reason = params != null ? params.get("reason") : null;
        orderService.adminCancelOrder(id, reason);
        return Result.success("订单已取消", null);
    }

    /**
     * 管理员处理退款申请
     */
    @PostMapping("/refund/{id}")
    public Result<?> processRefund(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        orderStatusFlowService.processRefund(order.getMerchantId(), id, approved, remark);
        return Result.success(approved ? "已同意退款" : "已拒绝退款");
    }
}

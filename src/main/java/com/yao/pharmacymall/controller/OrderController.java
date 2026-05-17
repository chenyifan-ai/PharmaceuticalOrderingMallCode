package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 订单控制器(C端)
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取用户订单列表
     */
    @GetMapping("/list")
    public Result<PageResult<Order>> getOrderList(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long userId = (Long) request.getAttribute("userId");
        PageResult<Order> result = orderService.getUserOrders(userId, status, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{id}")
    public Result<Order> getOrderDetail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.getOrderDetail(userId, id);
        return Result.success(order);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{id}")
    public Result<?> cancelOrder(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        String reason = params.get("reason");
        orderService.cancelOrder(userId, id, reason);
        return Result.success("订单已取消", null);
    }

    /**
     * 确认收货
     */
    @PostMapping("/confirmReceive/{id}")
    public Result<?> confirmReceive(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.confirmReceive(userId, id);
        return Result.success("确认收货成功", null);
    }
}

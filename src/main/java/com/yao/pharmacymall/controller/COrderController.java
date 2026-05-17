package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.OtcOrderRequest;
import com.yao.pharmacymall.dto.PackageOrderRequest;
import com.yao.pharmacymall.dto.PrescriptionOrderRequest;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.dto.LogisticsTraceItemVO;
import com.yao.pharmacymall.service.LogisticsTraceService;
import com.yao.pharmacymall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * C端订单控制器
 */
@RestController
@RequestMapping("/api/c/order")
public class COrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LogisticsTraceService logisticsTraceService;

    /**
     * 创建OTC订单
     */
    @PostMapping("/create-otc")
    public Result<Order> createOtcOrder(HttpServletRequest request, @Valid @RequestBody OtcOrderRequest body) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.createOtcOrder(
                userId,
                body.getCartItemIds(),
                body.getAddressId(),
                body.getMedicationUserId(),
                body.getRemark(),
                body.getUserCouponId()
        ));
    }

    /**
     * 创建套餐订单
     */
    @PostMapping("/create-package")
    public Result<Order> createPackageOrder(HttpServletRequest request, @Valid @RequestBody PackageOrderRequest body) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.createPackageOrder(
                userId,
                body.getPackageId(),
                body.getAddressId(),
                body.getMedicationUserId(),
                body.getRemark()
        ));
    }

    /**
     * 创建处方药订单
     */
    @PostMapping("/create-prescription")
    public Result<Order> createPrescriptionOrder(
            HttpServletRequest request,
            @Valid @RequestBody PrescriptionOrderRequest body) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.createPrescriptionOrder(userId, body));
    }

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
    @GetMapping("/{id}")
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
        orderService.cancelOrder(userId, id, params.get("reason"));
        return Result.success("取消成功", null);
    }

    /**
     * 确认收货
     */
    @PostMapping("/confirm/{id}")
    public Result<?> confirmReceive(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.confirmReceive(userId, id);
        return Result.success("确认收货成功", null);
    }

    @GetMapping("/{id}/logistics")
    public Result<java.util.List<LogisticsTraceItemVO>> logistics(
            HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(logisticsTraceService.getTraceForUser(userId, id));
    }
}

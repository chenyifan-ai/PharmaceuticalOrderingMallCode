package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.service.OrderService;
import com.yao.pharmacymall.service.OrderStatusFlowService;
import com.yao.pharmacymall.util.MerchantAuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 供应商订单控制器(B端)
 */
@RestController
@RequestMapping("/api/merchant/order")
public class MerchantOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStatusFlowService orderStatusFlowService;

    /**
     * 获取供应商订单列表
     */
    @GetMapping("/list")
    public Result<PageResult<Order>> getOrderList(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        PageResult<Order> result = orderService.getSupplierOrders(merchantId, status, page, pageSize);
        return Result.success(result);
    }

    /**
     * 发货
     */
    @PostMapping("/ship/{id}")
    public Result<?> shipOrder(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, String> params) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        String logisticsCompany = params.get("logisticsCompany");
        String logisticsNo = params.get("logisticsNo");
        orderService.shipOrder(merchantId, id, logisticsCompany, logisticsNo);
        return Result.success("发货成功", null);
    }

    /**
     * 供应商处理退款
     */
    @PostMapping("/refund/{id}")
    public Result<?> processRefund(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        orderStatusFlowService.processRefund(merchantId, id, approved, remark);
        return Result.success(approved ? "已同意退款" : "已拒绝退款");
    }
}
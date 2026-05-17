package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.entity.OrderStatusLog;
import com.yao.pharmacymall.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 订单状态流转服务
 * 管理订单状态的合法流转和记录
 */
@Slf4j
@Service
public class OrderStatusFlowService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStatusLogService orderStatusLogService;

    @Autowired
    private LogisticsTraceService logisticsTraceService;

    public OrderStatusLogService getOrderStatusLogService() {
        return orderStatusLogService;
    }

    // 定义合法的状态流转规则
    // key: 当前状态, value: 允许流转到的下一个状态列表
    private static final Map<Integer, List<Integer>> VALID_TRANSITIONS = Map.of(
        // 待付款 -> 待审核(处方药支付后), 待发货(OTC支付后), 已取消
        OrderStatus.PENDING_PAYMENT.getCode(), Arrays.asList(
            OrderStatus.PENDING_AUDIT.getCode(),
            OrderStatus.PENDING_SHIPMENT.getCode(),
            OrderStatus.CANCELLED.getCode()
        ),
        // 待审核 -> 待发货(审核通过), 已取消(审核拒绝)
        OrderStatus.PENDING_AUDIT.getCode(), Arrays.asList(
            OrderStatus.PENDING_SHIPMENT.getCode(),
            OrderStatus.CANCELLED.getCode()
        ),
        // 待发货 -> 已发货
        OrderStatus.PENDING_SHIPMENT.getCode(), Arrays.asList(
            OrderStatus.SHIPPED.getCode()
        ),
        // 已发货 -> 已完成, 退款中
        OrderStatus.SHIPPED.getCode(), Arrays.asList(
            OrderStatus.COMPLETED.getCode(),
            OrderStatus.REFUNDING.getCode()
        ),
        // 退款中 -> 已退款, 待发货(退款拒绝)
        OrderStatus.REFUNDING.getCode(), Arrays.asList(
            OrderStatus.REFUNDED.getCode(),
            OrderStatus.PENDING_SHIPMENT.getCode()
        )
    );

    /**
     * 变更订单状态(带状态流转校验)
     *
     * @param orderId     订单ID
     * @param newStatus   新状态
     * @param operatorId  操作人ID
     * @param operatorType 操作人类型: 1-用户, 2-商家, 3-管理员, 4-系统
     * @param remark      备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeOrderStatus(Long orderId, Integer newStatus, Long operatorId, 
                                   Integer operatorType, String remark) {
        // 1. 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        Integer oldStatus = order.getStatus();

        // 2. 验证状态流转是否合法
        validateStatusTransition(oldStatus, newStatus);

        // 3. 更新订单状态
        order.setStatus(newStatus);
        
        // 根据新状态设置相应的时间字段
        setTimestampByStatus(order, newStatus);
        
        orderService.updateById(order);

        // 4. 记录状态变更日志
        saveStatusLog(order, oldStatus, newStatus, operatorId, operatorType, remark);

        log.info("订单状态变更成功: orderId={}, {} -> {}, operatorId={}", 
                orderId, getOrderStatusDesc(oldStatus), getOrderStatusDesc(newStatus), operatorId);
    }

    /**
     * 验证状态流转是否合法
     */
    private void validateStatusTransition(Integer fromStatus, Integer toStatus) {
        if (fromStatus.equals(toStatus)) {
            throw new BusinessException("状态未发生变化");
        }

        List<Integer> allowedStatuses = VALID_TRANSITIONS.get(fromStatus);
        if (allowedStatuses == null || !allowedStatuses.contains(toStatus)) {
            throw new BusinessException(String.format(
                "不允许的状态流转: %s -> %s",
                getOrderStatusDesc(fromStatus),
                getOrderStatusDesc(toStatus)
            ));
        }
    }

    /**
     * 根据状态设置相应的时间字段
     */
    private void setTimestampByStatus(Order order, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (status) {
            case 0: // 待付款
                // 无需设置时间
                break;
            case 1: // 待审核
                // 支付成功后设置
                order.setPayTime(now);
                break;
            case 2: // 待发货
                if (order.getPayTime() == null) {
                    order.setPayTime(now);
                }
                break;
            case 3: // 已发货
                order.setShipTime(now);
                break;
            case 4: // 已完成
                order.setReceiveTime(now);
                break;
            case 5: // 已取消
                order.setCancelTime(now);
                break;
            case 6: // 退款中
                // 无需设置时间
                break;
            case 7: // 已退款
                // 无需设置时间
                break;
            default:
                log.warn("未知的订单状态: {}", status);
        }
    }

    /**
     * 保存状态变更日志
     */
    private void saveStatusLog(Order order, Integer oldStatus, Integer newStatus,
                                Long operatorId, Integer operatorType, String remark) {
        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setOrderId(order.getId());
        statusLog.setOldStatus(oldStatus);
        statusLog.setNewStatus(newStatus);
        statusLog.setOperatorId(operatorId);
        statusLog.setOperatorType(operatorType);
        statusLog.setRemark(remark);
        statusLog.setCreateTime(LocalDateTime.now());
        
        orderStatusLogService.save(statusLog);
    }

    /**
     * 获取订单状态描述
     */
    private String getOrderStatusDesc(Integer status) {
        OrderStatus orderStatus = OrderStatus.getByCode(status);
        return orderStatus != null ? orderStatus.getDesc() : "未知状态";
    }

    /**
     * 用户取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderByUser(Long userId, Long orderId, String reason) {
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        changeOrderStatus(orderId, OrderStatus.CANCELLED.getCode(), 
                         userId, 1, "用户取消订单: " + reason);

        // 恢复库存
        orderService.restoreStock(orderId);
    }

    /**
     * 商家取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderByMerchant(Long merchantId, Long orderId, String reason) {
        Order order = orderService.getById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("订单不存在");
        }

        changeOrderStatus(orderId, OrderStatus.CANCELLED.getCode(),
                         merchantId, 2, "商家取消订单: " + reason);

        // 恢复库存
        orderService.restoreStock(orderId);
    }

    /**
     * 商家发货
     */
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long merchantId, Long orderId, String logisticsCompany, String logisticsNo) {
        Order order = orderService.getById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("订单不存在");
        }

        changeOrderStatus(orderId, OrderStatus.SHIPPED.getCode(),
                         merchantId, 2, "商家发货: " + logisticsCompany + " " + logisticsNo);

        // 更新物流信息
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNo(logisticsNo);
        orderService.updateById(order);

        logisticsTraceService.seedShipTrace(order);
    }

    /**
     * 用户确认收货
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long userId, Long orderId) {
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        changeOrderStatus(orderId, OrderStatus.COMPLETED.getCode(),
                         userId, 1, "用户确认收货");
    }

    /**
     * 申请退款
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long userId, Long orderId, String reason) {
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        changeOrderStatus(orderId, OrderStatus.REFUNDING.getCode(),
                         userId, 1, "申请退款: " + reason);
    }

    /**
     * 处理退款(同意退款)
     */
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Long merchantId, Long orderId, boolean approved, String remark) {
        Order order = orderService.getById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getStatus().equals(OrderStatus.REFUNDING.getCode())) {
            throw new BusinessException("订单不在退款中状态");
        }

        if (approved) {
            changeOrderStatus(orderId, OrderStatus.REFUNDED.getCode(),
                             merchantId, 2, "同意退款: " + remark);
            // TODO: 调用支付接口退款
        } else {
            changeOrderStatus(orderId, OrderStatus.PENDING_SHIPMENT.getCode(),
                             merchantId, 2, "拒绝退款: " + remark);
        }
    }
}

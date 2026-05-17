package com.yao.pharmacymall.config;

import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.entity.OrderStatusLog;
import com.yao.pharmacymall.enums.OrderStatus;
import com.yao.pharmacymall.service.OrderService;
import com.yao.pharmacymall.service.OrderStatusLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 为已有订单补充状态变更记录（演示/联调用）
 */
@Slf4j
@Component
@org.springframework.core.annotation.Order(30)
public class OrderStatusLogSeedInitializer implements CommandLineRunner {

    @Autowired
    private OrderStatusLogService orderStatusLogService;
    @Autowired
    private OrderService orderService;

    @Override
    public void run(String... args) {
        List<Order> orders = orderService.list();
        if (orders.isEmpty()) {
            return;
        }
        int seeded = 0;
        for (Order order : orders) {
            long exists = orderStatusLogService.lambdaQuery()
                    .eq(OrderStatusLog::getOrderId, order.getId())
                    .count();
            if (exists == 0) {
                seedLogsForOrder(order);
                seeded++;
            }
        }
        if (seeded > 0) {
            log.info("已为 {} 笔订单补充状态变更记录", seeded);
        }
    }

    private void seedLogsForOrder(Order order) {
        List<int[]> chain = buildStatusChain(order.getStatus());
        LocalDateTime base = order.getCreateTime() != null ? order.getCreateTime() : LocalDateTime.now().minusDays(2);
        int step = 0;
        for (int[] transition : chain) {
            OrderStatusLog log = new OrderStatusLog();
            log.setOrderId(order.getId());
            log.setOldStatus(transition[0]);
            log.setNewStatus(transition[1]);
            log.setOperatorId(1L);
            log.setOperatorType(transition[2]);
            log.setRemark(transition[3] == 1 ? remarkFor(transition[0], transition[1]) : null);
            log.setCreateTime(base.plusMinutes(step * 45L));
            orderStatusLogService.save(log);
            step++;
        }
    }

    /** [oldStatus, newStatus, operatorType, hasRemarkFlag] operatorType: 1用户 2商家 3管理员 4系统 */
    private List<int[]> buildStatusChain(Integer finalStatus) {
        if (finalStatus == null) {
            finalStatus = 0;
        }
        List<int[]> chain = new ArrayList<>();
        switch (finalStatus) {
            case 0:
                chain.add(entry(null, 0, 4, 1));
                break;
            case 1:
                chain.add(entry(null, 0, 4, 1));
                chain.add(entry(0, 1, 4, 1));
                break;
            case 2:
                chain.add(entry(null, 0, 4, 1));
                chain.add(entry(0, 2, 1, 1));
                break;
            case 3:
                chain.add(entry(null, 0, 4, 1));
                chain.add(entry(0, 2, 1, 1));
                chain.add(entry(2, 3, 2, 1));
                break;
            case 4:
                chain.add(entry(null, 0, 4, 1));
                chain.add(entry(0, 2, 1, 1));
                chain.add(entry(2, 3, 2, 1));
                chain.add(entry(3, 4, 1, 1));
                break;
            case 5:
                chain.add(entry(null, 0, 4, 1));
                chain.add(entry(0, 5, 1, 1));
                break;
            default:
                chain.add(entry(null, 0, 4, 1));
                chain.add(entry(0, finalStatus, 3, 1));
                break;
        }
        return chain;
    }

    private int[] entry(Integer old, int newStatus, int operatorType, int hasRemark) {
        return new int[]{old == null ? 0 : old, newStatus, operatorType, hasRemark};
    }

    private String remarkFor(int oldStatus, int newStatus) {
        if (oldStatus == newStatus && newStatus == 0) {
            return "用户提交订单";
        }
        if (newStatus == OrderStatus.PENDING_AUDIT.getCode()) {
            return "处方药订单，等待药师审核";
        }
        if (newStatus == OrderStatus.PENDING_SHIPMENT.getCode()) {
            return oldStatus == 0 ? "支付成功" : "审核通过，待发货";
        }
        if (newStatus == OrderStatus.SHIPPED.getCode()) {
            return "商家已发货";
        }
        if (newStatus == OrderStatus.COMPLETED.getCode()) {
            return "用户确认收货";
        }
        if (newStatus == OrderStatus.CANCELLED.getCode()) {
            return "订单已取消";
        }
        return "状态更新";
    }
}

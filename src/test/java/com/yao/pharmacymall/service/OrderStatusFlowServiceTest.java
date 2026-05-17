package com.yao.pharmacymall.service;

import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单状态流转服务测试类
 */
@SpringBootTest
@Transactional
public class OrderStatusFlowServiceTest {

    @Autowired
    private OrderStatusFlowService orderStatusFlowService;

    @Autowired
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // 创建测试订单(OTC)
        testOrder = new Order();
        testOrder.setOrderNo("TEST_ORD_" + System.currentTimeMillis());
        testOrder.setUserId(1L);
        testOrder.setMerchantId(1L);
        testOrder.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        testOrder.setOrderType(1); // OTC
        testOrder.setReceiverName("测试用户");
        testOrder.setReceiverPhone("13800138000");
        testOrder.setReceiverAddress("测试地址");
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setFreight(BigDecimal.ZERO);
        testOrder.setDiscountAmount(BigDecimal.ZERO);
        testOrder.setPayAmount(new BigDecimal("100.00"));
        orderService.save(testOrder);
    }

    @Test
    void testCancelOrderByUser() {
        // 用户取消待付款订单
        orderStatusFlowService.cancelOrderByUser(1L, testOrder.getId(), "不想要了");

        Order updatedOrder = orderService.getById(testOrder.getId());
        assertEquals(OrderStatus.CANCELLED.getCode(), updatedOrder.getStatus());
        assertNotNull(updatedOrder.getCancelTime());
    }

    @Test
    void testCancelOrderByUser_InvalidStatus() {
        // 先模拟支付成功,订单变为待发货
        testOrder.setStatus(OrderStatus.PENDING_SHIPMENT.getCode());
        orderService.updateById(testOrder);

        // 尝试取消已支付的订单,应该失败
        assertThrows(RuntimeException.class, () -> {
            orderStatusFlowService.cancelOrderByUser(1L, testOrder.getId(), "不想要了");
        });
    }

    @Test
    void testShipOrder() {
        // 先将订单状态改为待发货
        testOrder.setStatus(OrderStatus.PENDING_SHIPMENT.getCode());
        orderService.updateById(testOrder);

        // 商家发货
        orderStatusFlowService.shipOrder(1L, testOrder.getId(), "顺丰快递", "SF123456789");

        Order updatedOrder = orderService.getById(testOrder.getId());
        assertEquals(OrderStatus.SHIPPED.getCode(), updatedOrder.getStatus());
        assertEquals("顺丰快递", updatedOrder.getLogisticsCompany());
        assertEquals("SF123456789", updatedOrder.getLogisticsNo());
        assertNotNull(updatedOrder.getShipTime());
    }

    @Test
    void testConfirmReceive() {
        // 先将订单状态改为已发货
        testOrder.setStatus(OrderStatus.SHIPPED.getCode());
        orderService.updateById(testOrder);

        // 用户确认收货
        orderStatusFlowService.confirmReceive(1L, testOrder.getId());

        Order updatedOrder = orderService.getById(testOrder.getId());
        assertEquals(OrderStatus.COMPLETED.getCode(), updatedOrder.getStatus());
        assertNotNull(updatedOrder.getReceiveTime());
    }

    @Test
    void testApplyRefund() {
        // 先将订单状态改为已发货
        testOrder.setStatus(OrderStatus.SHIPPED.getCode());
        orderService.updateById(testOrder);

        // 用户申请退款
        orderStatusFlowService.applyRefund(1L, testOrder.getId(), "商品有问题");

        Order updatedOrder = orderService.getById(testOrder.getId());
        assertEquals(OrderStatus.REFUNDING.getCode(), updatedOrder.getStatus());
    }

    @Test
    void testProcessRefund_Approved() {
        // 先将订单状态改为退款中
        testOrder.setStatus(OrderStatus.REFUNDING.getCode());
        orderService.updateById(testOrder);

        // 商家同意退款
        orderStatusFlowService.processRefund(1L, testOrder.getId(), true, "同意退款");

        Order updatedOrder = orderService.getById(testOrder.getId());
        assertEquals(OrderStatus.REFUNDED.getCode(), updatedOrder.getStatus());
    }

    @Test
    void testProcessRefund_Rejected() {
        // 先将订单状态改为退款中
        testOrder.setStatus(OrderStatus.REFUNDING.getCode());
        orderService.updateById(testOrder);

        // 商家拒绝退款
        orderStatusFlowService.processRefund(1L, testOrder.getId(), false, "商品无问题");

        Order updatedOrder = orderService.getById(testOrder.getId());
        assertEquals(OrderStatus.PENDING_SHIPMENT.getCode(), updatedOrder.getStatus());
    }

    @Test
    void testInvalidStatusTransition() {
        // 尝试从待付款直接跳到已完成,应该失败
        assertThrows(RuntimeException.class, () -> {
            orderStatusFlowService.changeOrderStatus(
                testOrder.getId(),
                OrderStatus.COMPLETED.getCode(),
                1L,
                1,
                "非法状态流转"
            );
        });
    }

    @Test
    void testGetStatusHistory() {
        // 先进行一些状态变更
        orderStatusFlowService.cancelOrderByUser(1L, testOrder.getId(), "不想要了");

        // 获取状态历史
        var history = orderStatusFlowService.getOrderStatusLogService().getOrderStatusHistory(testOrder.getId());
        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
    }
}

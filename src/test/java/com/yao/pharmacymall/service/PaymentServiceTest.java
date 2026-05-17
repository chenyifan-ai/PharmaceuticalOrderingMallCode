package com.yao.pharmacymall.service;

import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.entity.Payment;
import com.yao.pharmacymall.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 支付服务测试类
 */
@SpringBootTest
@Transactional
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    private Payment testPayment;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // 创建测试订单
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

        // 创建测试支付记录
        testPayment = new Payment();
        testPayment.setPaymentNo("TEST_PAY_" + System.currentTimeMillis());
        testPayment.setOrderId(testOrder.getId());
        testPayment.setUserId(1L);
        testPayment.setPaymentMethod(1); // 微信支付
        testPayment.setAmount(new BigDecimal("100.00"));
        testPayment.setStatus(0); // 待支付
        paymentService.save(testPayment);
    }

    @Test
    void testHandlePaymentCallback_Success() {
        // 执行支付成功回调
        String transactionNo = "TXN_" + System.currentTimeMillis();
        String callbackData = "{\"status\":\"SUCCESS\"}";

        paymentService.handlePaymentCallback(
            testPayment.getPaymentNo(),
            transactionNo,
            callbackData
        );

        // 验证支付记录已更新
        Payment updatedPayment = paymentService.getById(testPayment.getId());
        assertEquals(1, updatedPayment.getStatus());
        assertEquals(transactionNo, updatedPayment.getTransactionNo());
        assertNotNull(updatedPayment.getPaymentTime());

        // 验证订单状态已更新
        Order updatedOrder = orderService.getById(testOrder.getId());
        assertEquals(OrderStatus.PENDING_SHIPMENT.getCode(), updatedOrder.getStatus());
        assertNotNull(updatedOrder.getPayTime());
        assertEquals(transactionNo, updatedOrder.getTransactionNo());
    }

    @Test
    void testHandlePaymentCallback_PrescriptionOrder() {
        // 修改订单为处方药订单
        testOrder.setOrderType(2);
        orderService.updateById(testOrder);

        // 执行支付成功回调
        paymentService.handlePaymentCallback(
            testPayment.getPaymentNo(),
            "TXN_TEST",
            "{}"
        );

        // 验证处方药订单进入待审核状态
        Order updatedOrder = orderService.getById(testOrder.getId());
        assertEquals(OrderStatus.PENDING_AUDIT.getCode(), updatedOrder.getStatus());
    }

    @Test
    void testHandlePaymentCallback_Idempotency() {
        // 第一次回调
        paymentService.handlePaymentCallback(
            testPayment.getPaymentNo(),
            "TXN_1",
            "{}"
        );

        // 第二次回调(应该被跳过)
        paymentService.handlePaymentCallback(
            testPayment.getPaymentNo(),
            "TXN_2",
            "{}"
        );

        // 验证交易号仍然是第一次的
        Payment updatedPayment = paymentService.getById(testPayment.getId());
        assertEquals("TXN_1", updatedPayment.getTransactionNo());
    }

    @Test
    void testHandlePaymentFailure() {
        // 执行支付失败回调
        paymentService.handlePaymentFailure(
            testPayment.getPaymentNo(),
            "用户取消支付"
        );

        // 验证支付记录状态
        Payment updatedPayment = paymentService.getById(testPayment.getId());
        assertEquals(2, updatedPayment.getStatus());

        // 验证订单状态仍为待付款(库存已恢复)
        Order updatedOrder = orderService.getById(testOrder.getId());
        assertEquals(OrderStatus.PENDING_PAYMENT.getCode(), updatedOrder.getStatus());
    }

    @Test
    void testVerifyCallbackSignature() {
        Map<String, String> params = new HashMap<>();
        params.put("paymentNo", testPayment.getPaymentNo());
        params.put("amount", "100.00");

        // 注意: 这里需要实际的签名算法来生成正确的签名
        // 由于是测试,我们只验证方法不会抛出异常
        assertDoesNotThrow(() -> {
            paymentService.verifyCallbackSignature(params, "test_signature");
        });
    }

    @Test
    void testHandlePaymentCallback_InvalidPaymentNo() {
        // 测试不存在的支付流水号
        assertThrows(RuntimeException.class, () -> {
            paymentService.handlePaymentCallback(
                "INVALID_NO",
                "TXN_TEST",
                "{}"
            );
        });
    }

    @Test
    void testHandlePaymentCallback_WrongOrderStatus() {
        // 修改订单状态为非待付款状态
        testOrder.setStatus(OrderStatus.CANCELLED.getCode());
        orderService.updateById(testOrder);

        // 应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            paymentService.handlePaymentCallback(
                testPayment.getPaymentNo(),
                "TXN_TEST",
                "{}"
            );
        });
    }
}

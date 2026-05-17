package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.entity.Payment;
import com.yao.pharmacymall.enums.OrderStatus;
import com.yao.pharmacymall.mapper.PaymentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 支付服务类
 */
@Slf4j
@Service
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserCouponService userCouponService;

    // 支付密钥(实际应该从配置文件读取)
    private static final String PAYMENT_SECRET_KEY = "your_payment_secret_key";

    /**
     * 创建支付记录
     */
    @Transactional(rollbackFor = Exception.class)
    public Payment createPayment(Long userId, Long orderId, Integer paymentMethod) {
        // 查询订单
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单状态
        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT.getCode())) {
            throw new BusinessException("订单状态不正确，无法支付");
        }

        // 创建支付记录
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(order.getPayAmount());
        payment.setStatus(0); // 待支付

        this.save(payment);

        return payment;
    }

    /**
     * 确认支付成功（演示/前台支付页：同步完成支付并更新订单状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(Long userId, Long paymentId) {
        Payment payment = getPaymentStatus(userId, paymentId);
        if (payment.getStatus() != null && payment.getStatus() == 1) {
            return;
        }
        if (payment.getStatus() == null || payment.getStatus() != 0) {
            throw new BusinessException("支付状态异常，无法确认");
        }
        if (payment.getPaymentMethod() != null && payment.getPaymentMethod() == 4) {
            throw new BusinessException("对公转账请上传付款凭证，等待平台审核");
        }
        String transactionNo = "DEMO" + System.currentTimeMillis();
        completePaymentSuccess(payment, transactionNo, "{\"source\":\"confirm\"}");
    }

    /**
     * 提交对公转账付款凭证
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitTransferVoucher(Long userId, Long paymentId, String voucherUrl, String transferRemark) {
        Payment payment = getPaymentStatus(userId, paymentId);
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod() != 4) {
            throw new BusinessException("当前支付方式无需上传凭证");
        }
        if (!StringUtils.hasText(voucherUrl)) {
            throw new BusinessException("请上传付款凭证");
        }
        payment.setVoucherUrl(voucherUrl);
        payment.setTransferRemark(transferRemark);
        payment.setVoucherStatus(1);
        this.updateById(payment);
    }

    /**
     * 管理员审核对公转账凭证
     */
    @Transactional(rollbackFor = Exception.class)
    public void reviewTransferVoucher(Long paymentId, boolean approved, String rejectReason) {
        Payment payment = this.getById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        if (payment.getVoucherStatus() == null || payment.getVoucherStatus() != 1) {
            throw new BusinessException("该支付单不在待审核状态");
        }
        if (approved) {
            payment.setVoucherStatus(2);
            payment.setVoucherRejectReason(null);
            this.updateById(payment);
            completePaymentSuccess(payment, "TRANSFER" + System.currentTimeMillis(), "{\"source\":\"voucher\"}");
        } else {
            payment.setVoucherStatus(3);
            payment.setVoucherRejectReason(rejectReason);
            this.updateById(payment);
        }
    }

    public com.yao.pharmacymall.common.PageResult<Payment> listPendingVouchers(Integer page, Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Payment> pageInfo =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentMethod, 4);
        wrapper.eq(Payment::getVoucherStatus, 1);
        wrapper.orderByDesc(Payment::getCreateTime);
        com.baomidou.mybatisplus.core.metadata.IPage<Payment> result = this.page(pageInfo, wrapper);
        return com.yao.pharmacymall.common.PageResult.of(
                result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * 生成支付流水号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * 处理支付回调(支持微信、支付宝等第三方支付)
     * 需要保证幂等性,防止重复回调
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentCallback(String paymentNo, String transactionNo, String callbackData) {
        log.info("收到支付回调: paymentNo={}, transactionNo={}", paymentNo, transactionNo);

        // 1. 查询支付记录
        Payment payment = this.getOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getPaymentNo, paymentNo)
        );

        if (payment == null) {
            log.error("支付记录不存在: {}", paymentNo);
            throw new BusinessException("支付记录不存在");
        }

        // 2. 检查是否已经处理过(幂等性校验)
        if (payment.getStatus() == 1) {
            log.warn("支付记录已处理,跳过: {}", paymentNo);
            return;
        }

        // 3. 检查订单状态
        Order order = orderService.getById(payment.getOrderId());
        if (order == null) {
            log.error("关联订单不存在: orderId={}", payment.getOrderId());
            throw new BusinessException("关联订单不存在");
        }

        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT.getCode())) {
            log.warn("订单状态不正确,无法处理支付: orderId={}, status={}", order.getId(), order.getStatus());
            throw new BusinessException("订单状态不正确,无法处理支付");
        }

        try {
            completePaymentSuccess(payment, transactionNo, callbackData);
            log.info("支付回调处理成功: paymentNo={}, orderId={}", paymentNo, payment.getOrderId());
        } catch (Exception e) {
            log.error("支付回调处理失败: paymentNo={}, error={}", paymentNo, e.getMessage(), e);
            throw new BusinessException("支付回调处理失败: " + e.getMessage());
        }
    }

    /**
     * 支付成功后的统一处理：更新支付单、订单状态、销量
     */
    private void completePaymentSuccess(Payment payment, String transactionNo, String callbackData) {
        if (payment.getStatus() != null && payment.getStatus() == 1) {
            return;
        }

        Order order = orderService.getById(payment.getOrderId());
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }
        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT.getCode())) {
            throw new BusinessException("订单状态不正确，无法完成支付");
        }

        payment.setStatus(1);
        payment.setTransactionNo(transactionNo);
        payment.setCallbackTime(LocalDateTime.now());
        payment.setCallbackData(callbackData);
        payment.setPaymentTime(LocalDateTime.now());
        this.updateById(payment);

        if (order.getOrderType() != null && order.getOrderType() == 2) {
            order.setStatus(OrderStatus.PENDING_AUDIT.getCode());
            log.info("处方药订单支付成功,进入待审核: orderId={}", order.getId());
        } else {
            order.setStatus(OrderStatus.PENDING_SHIPMENT.getCode());
            log.info("订单支付成功,进入待发货: orderId={}", order.getId());
        }

        order.setPayTime(LocalDateTime.now());
        order.setPayType(payment.getPaymentMethod());
        order.setTransactionNo(transactionNo);
        orderService.updateById(order);

        if (order.getUserCouponId() != null) {
            userCouponService.useCoupon(order.getUserCouponId());
        }

        updateProductSales(order.getId());
    }

    /**
     * 处理支付失败回调
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentFailure(String paymentNo, String failReason) {
        log.warn("收到支付失败回调: paymentNo={}, reason={}", paymentNo, failReason);

        Payment payment = this.getOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getPaymentNo, paymentNo)
        );

        if (payment == null) {
            log.error("支付记录不存在: {}", paymentNo);
            return;
        }

        // 更新支付状态为失败
        payment.setStatus(2); // 支付失败
        payment.setCallbackTime(LocalDateTime.now());
        payment.setCallbackData(failReason);
        this.updateById(payment);

        // 恢复库存
        restoreStock(payment.getOrderId());

        log.info("支付失败处理完成: paymentNo={}", paymentNo);
    }

    /**
     * 验证支付回调签名(防止伪造回调)
     */
    public boolean verifyCallbackSignature(Map<String, String> params, String signature) {
        if (params == null || params.isEmpty()) {
            return false;
        }

        // 1. 参数排序
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        // 2. 拼接签名字符串
        StringBuilder signStr = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                signStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        signStr.append("key=").append(PAYMENT_SECRET_KEY);

        // 3. MD5加密
        String calculatedSign = md5(signStr.toString());

        log.debug("签名验证: calculated={}, received={}", calculatedSign, signature);

        return calculatedSign.equalsIgnoreCase(signature);
    }

    /**
     * MD5加密
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("MD5加密失败", e);
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * 更新商品销量
     */
    private void updateProductSales(Long orderId) {
        try {
            orderService.updateProductSales(orderId);
        } catch (Exception e) {
            log.error("更新商品销量失败: orderId={}", orderId, e);
            // 不影响主流程,只记录日志
        }
    }

    /**
     * 恢复库存(支付失败或订单取消时调用)
     */
    private void restoreStock(Long orderId) {
        try {
            orderService.restoreStock(orderId);
        } catch (Exception e) {
            log.error("恢复库存失败: orderId={}", orderId, e);
        }
    }

    /**
     * 查询支付状态
     */
    public Payment getPaymentStatus(Long userId, Long paymentId) {
        Payment payment = this.getById(paymentId);
        if (payment == null || !payment.getUserId().equals(userId)) {
            throw new BusinessException("支付记录不存在");
        }
        return payment;
    }
}

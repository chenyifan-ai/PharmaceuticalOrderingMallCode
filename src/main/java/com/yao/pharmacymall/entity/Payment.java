package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment")
public class Payment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付方式: 1-微信, 2-支付宝, 3-银联, 4-医保
     */
    private Integer paymentMethod;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态: 0-待支付, 1-支付成功, 2-支付失败, 3-已退款
     */
    private Integer status;

    /**
     * 第三方交易号
     */
    private String transactionNo;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 回调时间
     */
    private LocalDateTime callbackTime;

    /**
     * 回调数据(JSON)
     */
    private String callbackData;

    /** 对公转账凭证图片 URL */
    private String voucherUrl;

    /** 凭证审核: 0-无, 1-待审核, 2-已通过, 3-已驳回 */
    private Integer voucherStatus;

    private String voucherRejectReason;

    private String transferRemark;
}

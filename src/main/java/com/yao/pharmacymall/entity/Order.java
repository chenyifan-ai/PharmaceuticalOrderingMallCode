package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`order`")
public class Order extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 订单状态: 0-待付款, 1-待审核(处方药), 2-待发货, 3-已发货, 4-已完成, 5-已取消, 6-退款中, 7-已退款
     */
    private Integer status;

    /**
     * 订单类型: 1-OTC, 2-处方药, 3-套餐
     */
    private Integer orderType;

    /**
     * 套餐ID（套餐订单）
     */
    private Long packageId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 用药人ID
     */
    private Long medicationUserId;

    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 运费
     */
    private BigDecimal freight;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /** 使用的用户优惠券 ID */
    private Long userCouponId;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式: 1-微信, 2-支付宝, 3-银联, 4-对公转账, 5-账期
     */
    private Integer payType;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 税号
     */
    private String invoiceTaxNo;

    /**
     * 发票状态: 0-未开票, 1-已开票
     */
    private Integer invoiceStatus;

    /**
     * 订单商品项（非数据库字段）
     */
    @TableField(exist = false)
    private List<OrderItem> orderItems;

    /**
     * 关联发票记录（非数据库字段，来自 invoice 表）
     */
    @TableField(exist = false)
    private Invoice invoice;
}

package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 优惠券实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
public class Coupon extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 类型：DISCOUNT-折扣券，CASH-现金券
     */
    private String type;

    /**
     * 折扣值
     */
    private BigDecimal discountValue;

    /**
     * 最低订单金额
     */
    private BigDecimal minOrderAmount;

    /**
     * 总发行量
     */
    private Integer totalCount;

    /**
     * 已领取数量
     */
    private Integer receivedCount;

    /**
     * 有效开始日期
     */
    private LocalDate validStartDate;

    /**
     * 有效结束日期
     */
    private LocalDate validEndDate;

    /**
     * 状态：ACTIVE-启用，INACTIVE-禁用
     */
    private Integer status;
}
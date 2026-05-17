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
 * 发票实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("invoice")
public class Invoice extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发票号
     */
    private String invoiceNo;

    /**
     * 订单ID
     */
    private Long orderId;

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
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 纳税人识别号
     */
    private String taxNumber;

    /**
     * 发票类型: 1-个人, 2-企业
     */
    private Integer invoiceType;

    /**
     * 发票内容
     */
    private String invoiceContent;

    /**
     * 发票金额
     */
    private BigDecimal amount;

    /**
     * 发票状态: 0-待开票, 1-已开票, 2-已寄送, 3-已作废
     */
    private Integer status;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人手机号
     */
    private String receiverPhone;

    /**
     * 收件地址
     */
    private String receiverAddress;

    /**
     * 快递公司
     */
    private String logisticsCompany;

    /**
     * 快递单号
     */
    private String logisticsNo;

    /**
     * 开票时间
     */
    private LocalDateTime invoiceTime;

    /**
     * 寄送时间
     */
    private LocalDateTime sendTime;

    /**
     * 备注
     */
    private String remark;
}

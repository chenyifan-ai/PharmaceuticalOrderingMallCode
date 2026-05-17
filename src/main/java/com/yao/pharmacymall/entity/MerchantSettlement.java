package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_settlement")
public class MerchantSettlement extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;
    private String settlementNo;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private BigDecimal platformFee;
    private BigDecimal settleAmount;
    /** 0-待结算 1-已结算 */
    private Integer status;
    private String remark;
    private LocalDateTime settleTime;
}

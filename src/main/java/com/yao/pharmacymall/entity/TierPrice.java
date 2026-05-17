package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 阶梯价格实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tier_price")
public class TierPrice extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 最小数量
     */
    private Integer minQuantity;

    /**
     * 最大数量(null表示无上限)
     */
    private Integer maxQuantity;

    /**
     * 价格
     */
    private BigDecimal price;
}
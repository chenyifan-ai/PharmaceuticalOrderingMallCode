package com.yao.pharmacymall.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 阶梯价格DTO
 */
@Data
public class TierPrice {

    /**
     * 最小数量(包含)
     */
    private Integer minQuantity;

    /**
     * 最大数量(不包含,null表示无上限)
     */
    private Integer maxQuantity;

    /**
     * 对应单价
     */
    private BigDecimal price;

    public TierPrice() {
    }

    public TierPrice(Integer minQuantity, Integer maxQuantity, BigDecimal price) {
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.price = price;
    }

    /**
     * 判断给定数量是否在此阶梯范围内
     */
    public boolean isInRange(Integer quantity) {
        if (quantity == null) {
            return false;
        }
        boolean minMatch = quantity >= minQuantity;
        boolean maxMatch = maxQuantity == null || quantity < maxQuantity;
        return minMatch && maxMatch;
    }
}

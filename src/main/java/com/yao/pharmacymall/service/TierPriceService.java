package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.pharmacymall.entity.TierPrice;

import java.math.BigDecimal;
import java.util.List;

public interface TierPriceService extends IService<TierPrice> {
    /**
     * 根据商品ID获取阶梯价格列表
     */
    List<TierPrice> getListByProductId(Long productId);

    /**
     * 根据商品ID和数量获取对应的价格
     */
    BigDecimal getPriceByQuantity(Long productId, Integer quantity);

    /**
     * 批量保存商品阶梯价格
     */
    Boolean saveBatchByProductId(Long productId, List<TierPrice> tierPrices);
}
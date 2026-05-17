package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.pharmacymall.entity.ProductStock;

public interface ProductStockService extends IService<ProductStock> {
    /**
     * 根据商品ID获取库存信息
     */
    ProductStock getByProductId(Long productId);

    /**
     * 增加库存
     */
    Boolean increaseStock(Long productId, Integer quantity);

    /**
     * 减少库存
     */
    Boolean decreaseStock(Long productId, Integer quantity);

    /**
     * 检查库存是否充足
     */
    Boolean checkStock(Long productId, Integer quantity);
}
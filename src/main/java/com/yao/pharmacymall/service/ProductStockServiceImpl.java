package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.ProductStock;
import com.yao.pharmacymall.mapper.ProductStockMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductStockServiceImpl extends ServiceImpl<ProductStockMapper, ProductStock> implements ProductStockService {

    @Override
    public ProductStock getByProductId(Long productId) {
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getProductId, productId);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional
    public Boolean increaseStock(Long productId, Integer quantity) {
        ProductStock stock = getByProductId(productId);
        if (stock == null) {
            // 如果没有库存记录，则创建新的库存记录
            stock = new ProductStock();
            stock.setProductId(productId);
            stock.setQuantity(quantity);
            stock.setLockedStock(0);
            return this.save(stock);
        } else {
            LambdaUpdateWrapper<ProductStock> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ProductStock::getProductId, productId)
                    .setSql("quantity = quantity + " + quantity);
            return this.update(updateWrapper);
        }
    }

    @Override
    @Transactional
    public Boolean decreaseStock(Long productId, Integer quantity) {
        ProductStock stock = getByProductId(productId);
        if (stock == null || stock.getQuantity() < quantity) {
            return false; // 库存不足
        }

        LambdaUpdateWrapper<ProductStock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductStock::getProductId, productId)
                .setSql("quantity = quantity - " + quantity);
        return this.update(updateWrapper);
    }

    @Override
    public Boolean checkStock(Long productId, Integer quantity) {
        ProductStock stock = getByProductId(productId);
        return stock != null && stock.getQuantity() >= quantity;
    }
}
package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.TierPrice;
import com.yao.pharmacymall.mapper.TierPriceMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TierPriceServiceImpl extends ServiceImpl<TierPriceMapper, TierPrice> implements TierPriceService {

    @Override
    public List<TierPrice> getListByProductId(Long productId) {
        LambdaQueryWrapper<TierPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TierPrice::getProductId, productId)
               .orderByAsc(TierPrice::getMinQuantity);
        return this.list(wrapper);
    }

    @Override
    public BigDecimal getPriceByQuantity(Long productId, Integer quantity) {
        List<TierPrice> tierPrices = getListByProductId(productId);
        
        // 按照数量找到对应的价格
        for (TierPrice tierPrice : tierPrices) {
            if (quantity >= tierPrice.getMinQuantity()) {
                // 如果设置了最大数量且当前数量超过最大数量，则跳过
                if (tierPrice.getMaxQuantity() != null && quantity > tierPrice.getMaxQuantity()) {
                    continue;
                }
                return tierPrice.getPrice();
            }
        }
        
        // 如果没有找到匹配的阶梯价格，返回null或者可以返回商品的基本价格
        return null;
    }

    @Override
    public Boolean saveBatchByProductId(Long productId, List<TierPrice> tierPrices) {
        // 先删除该商品的所有阶梯价格
        LambdaQueryWrapper<TierPrice> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(TierPrice::getProductId, productId);
        this.remove(deleteWrapper);
        
        // 再批量插入新的阶梯价格
        for (TierPrice tierPrice : tierPrices) {
            tierPrice.setProductId(productId);
        }
        
        return this.saveBatch(tierPrices);
    }
}
package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.SeckillItem;
import com.yao.pharmacymall.mapper.SeckillItemMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 秒杀活动校验与库存
 */
@Service
public class SeckillService extends ServiceImpl<SeckillItemMapper, SeckillItem> {

    /**
     * 校验秒杀活动有效，并检查剩余库存是否满足购买数量
     */
    public SeckillItem requireActiveSeckill(Long seckillId, Long productId, int quantity) {
        if (seckillId == null || seckillId <= 0) {
            throw new BusinessException("秒杀活动无效");
        }
        SeckillItem item = getById(seckillId);
        if (item == null || item.getStatus() == null || item.getStatus() != 1) {
            throw new BusinessException("秒杀活动不存在或已结束");
        }
        if (!item.getProductId().equals(productId)) {
            throw new BusinessException("秒杀活动与商品不匹配");
        }
        LocalDateTime now = LocalDateTime.now();
        if (item.getStartTime() != null && now.isBefore(item.getStartTime())) {
            throw new BusinessException("秒杀活动尚未开始");
        }
        if (item.getEndTime() != null && now.isAfter(item.getEndTime())) {
            throw new BusinessException("秒杀活动已结束");
        }
        int sold = item.getSoldCount() == null ? 0 : item.getSoldCount();
        int stock = item.getStock() == null ? 0 : item.getStock();
        int remain = stock - sold;
        if (remain < quantity) {
            throw new BusinessException("秒杀库存不足");
        }
        return item;
    }

    /**
     * 下单成功后扣减秒杀可售量（增加已售）
     */
    public void increaseSold(Long seckillId, int quantity) {
        SeckillItem item = getById(seckillId);
        if (item == null) {
            return;
        }
        int sold = item.getSoldCount() == null ? 0 : item.getSoldCount();
        item.setSoldCount(sold + quantity);
        updateById(item);
    }

    /**
     * 取消订单时恢复秒杀可售量
     */
    public void decreaseSold(Long seckillId, int quantity) {
        if (seckillId == null || seckillId <= 0 || quantity <= 0) {
            return;
        }
        SeckillItem item = getById(seckillId);
        if (item == null) {
            return;
        }
        int sold = item.getSoldCount() == null ? 0 : item.getSoldCount();
        item.setSoldCount(Math.max(0, sold - quantity));
        updateById(item);
    }
}

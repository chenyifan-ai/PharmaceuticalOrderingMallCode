package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.OrderItem;
import com.yao.pharmacymall.mapper.OrderItemMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单商品项服务类
 */
@Service
public class OrderItemService extends ServiceImpl<OrderItemMapper, OrderItem> {

    /**
     * 获取订单的商品项列表
     */
    public List<OrderItem> getOrderItems(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return this.list(wrapper);
    }
}

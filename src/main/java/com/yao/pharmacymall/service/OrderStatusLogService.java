package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.OrderStatusLog;
import com.yao.pharmacymall.mapper.OrderStatusLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单状态日志服务类
 */
@Service
public class OrderStatusLogService extends ServiceImpl<OrderStatusLogMapper, OrderStatusLog> {

    /**
     * 获取订单的状态变更历史
     */
    public List<OrderStatusLog> getOrderStatusHistory(Long orderId) {
        LambdaQueryWrapper<OrderStatusLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderStatusLog::getOrderId, orderId);
        wrapper.orderByAsc(OrderStatusLog::getCreateTime);
        return this.list(wrapper);
    }
}

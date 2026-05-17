package com.yao.pharmacymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.pharmacymall.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单状态日志Mapper
 */
@Mapper
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
}

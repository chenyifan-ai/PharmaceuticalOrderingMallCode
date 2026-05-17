package com.yao.pharmacymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.pharmacymall.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录Mapper接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}

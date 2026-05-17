package com.yao.pharmacymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.pharmacymall.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车项Mapper接口
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}

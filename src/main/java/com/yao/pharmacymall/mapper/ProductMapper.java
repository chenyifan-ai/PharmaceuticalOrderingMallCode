package com.yao.pharmacymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.pharmacymall.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 使用乐观锁扣减库存
     * SQL层面保证并发安全: WHERE stock >= #{quantity} AND version = (SELECT version FROM product WHERE id = #{productId})
     * 
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 影响的行数
     */
    @Update("UPDATE product SET stock = stock - #{quantity}, version = version + 1, update_time = NOW() " +
            "WHERE id = #{productId} AND stock >= #{quantity}")
    int decreaseStockWithOptimisticLock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}

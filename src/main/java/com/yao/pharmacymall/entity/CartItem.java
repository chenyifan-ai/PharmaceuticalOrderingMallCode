package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车项实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart_item")
public class CartItem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀活动ID，0 表示非秒杀
     */
    private Long seckillId;

    /**
     * 商品名称(快照)
     */
    private String productName;

    /**
     * 商品图片(快照)
     */
    private String productImage;

    /**
     * 商品价格(快照)
     */
    private java.math.BigDecimal price;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 是否选中: 0-否, 1-是
     */
    private Integer checked;
}

package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品库存实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_stock")
public class ProductStock extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 库存数量
     */
    private Integer quantity;

    /**
     * 库存预警值
     */
    private Integer warningQuantity;

    /**
     * 批号
     */
    private String batchNumber;

    /**
     * 生产日期
     */
    private LocalDate productionDate;

    /**
     * 有效期
     */
    private LocalDate expireDate;

    /**
     * 锁定库存
     */
    private Integer lockedStock;
}
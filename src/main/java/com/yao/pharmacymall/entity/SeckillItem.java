package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seckill_item")
public class SeckillItem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private Long productId;
    private BigDecimal seckillPrice;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer soldCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer sort;
    private Integer status;
}

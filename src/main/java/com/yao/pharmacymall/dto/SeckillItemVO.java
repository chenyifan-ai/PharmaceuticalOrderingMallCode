package com.yao.pharmacymall.dto;

import com.yao.pharmacymall.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillItemVO {
    private Long id;
    private String title;
    private Long productId;
    private BigDecimal seckillPrice;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer soldCount;
    private LocalDateTime endTime;
    private Integer progressPercent;
    private Product product;
}

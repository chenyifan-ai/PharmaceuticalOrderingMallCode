package com.yao.pharmacymall.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存管理列表项
 */
@Data
public class StockListVO {

    private Long productId;

    private String productName;

    private String mainImage;

    private String brand;

    /** 生产厂家 */
    private String manufacturer;

    private String specification;

    private Integer stock;

    private Integer sales;

    private Integer status;

    private Long supplierId;

    private BigDecimal wholesalePrice;

    /** 预警阈值 */
    private Integer warningQuantity;

    private Integer lockedStock;

    /** NORMAL / LOW / OUT */
    private String stockStatus;
}

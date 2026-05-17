package com.yao.pharmacymall.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品查询请求DTO
 */
@Data
public class ProductQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 20;

    private String keyword;

    private Long categoryId;

    private String prescriptionType;

    private String brand;

    private Long brandId;

    private String dosageForm;

    private String manufacturer;

    private Long supplierId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String sortBy = "comprehensive";

    private Boolean inStockOnly = false;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向：asc/desc
     */
    private String orderDir;
}

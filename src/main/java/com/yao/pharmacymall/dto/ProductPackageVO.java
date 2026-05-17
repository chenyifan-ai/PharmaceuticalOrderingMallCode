package com.yao.pharmacymall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductPackageVO {
    private Long id;
    private String packageName;
    private String subtitle;
    private String bannerImage;
    private BigDecimal originalPrice;
    private BigDecimal packagePrice;
    private Integer stock;
    private Integer discountPercent;
    private List<PackageItemDTO> items;
}

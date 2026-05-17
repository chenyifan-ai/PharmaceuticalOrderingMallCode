package com.yao.pharmacymall.dto;

import lombok.Data;

@Data
public class PackageItemDTO {
    private Long productId;
    private Integer quantity;
    private String productName;
    private String specification;
    private String image;
}

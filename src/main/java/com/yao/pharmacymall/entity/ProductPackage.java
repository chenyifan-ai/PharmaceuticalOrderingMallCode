package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_package")
public class ProductPackage extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String packageName;
    private String subtitle;
    private String bannerImage;
    private BigDecimal originalPrice;
    private BigDecimal packagePrice;
    /** JSON: [{"productId":1,"quantity":1,"productName":"..."}] */
    private String items;
    private Integer stock;
    private Integer sort;
    private Integer status;
}

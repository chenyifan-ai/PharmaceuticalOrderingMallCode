package com.yao.pharmacymall.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class StockWarningRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "预警值不能为空")
    @Min(value = 0, message = "预警值不能为负数")
    private Integer warningQuantity;
}

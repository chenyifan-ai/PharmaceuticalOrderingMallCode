package com.yao.pharmacymall.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class StockAdjustRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 1-入库 2-出库 3-盘点（设置为指定数量） */
    @NotNull(message = "调整类型不能为空")
    private Integer changeType;

    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能为负数")
    private Integer quantity;

    private String reason;
}

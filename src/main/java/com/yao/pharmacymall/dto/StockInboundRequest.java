package com.yao.pharmacymall.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StockInboundRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "进库数量不能为空")
    @Min(value = 1, message = "进库数量至少为 1")
    private Integer quantity;

    /** 批号（选填，填写则记入批次库存） */
    private String batchNumber;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    /** 进库备注，如采购单号、供应商等 */
    private String reason;
}

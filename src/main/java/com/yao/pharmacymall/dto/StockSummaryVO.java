package com.yao.pharmacymall.dto;

import lombok.Data;

@Data
public class StockSummaryVO {

    private Long totalSku;

    private Long lowStockCount;

    private Long outOfStockCount;

    private Long totalQuantity;
}

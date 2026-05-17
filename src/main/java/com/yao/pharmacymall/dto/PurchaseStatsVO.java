package com.yao.pharmacymall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseStatsVO {
    private Long orderCount;
    private BigDecimal totalPayAmount;
    private Long completedCount;
    private List<ProductRankItem> topProducts;

    @Data
    public static class ProductRankItem {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal amount;
    }
}

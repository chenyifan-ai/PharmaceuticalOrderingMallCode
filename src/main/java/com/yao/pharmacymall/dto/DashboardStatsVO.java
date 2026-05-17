package com.yao.pharmacymall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理端数据概览大屏统计
 */
@Data
public class DashboardStatsVO {

    private Integer orderTotal;
    private Integer orderCompleted;
    private BigDecimal revenue;
    private Integer productCount;
    private Integer merchantCount;
    private Integer userCount;
    private Double completionRate;
    private Double refundRate;

    private List<NameValue> categorySales = new ArrayList<>();
    private List<MonthValue> monthlyOrders = new ArrayList<>();
    private List<NameValue> productRanks = new ArrayList<>();
    private List<SalesTrendPoint> salesTrendWeek = new ArrayList<>();
    private List<SalesTrendPoint> salesTrendMonth = new ArrayList<>();
    private List<SalesTrendPoint> salesTrendQuarter = new ArrayList<>();
    private List<RecentOrderItem> recentOrders = new ArrayList<>();

    @Data
    public static class RecentOrderItem {
        private String orderNo;
        private String receiverName;
        private String receiverPhone;
        private String productName;
        private java.math.BigDecimal payAmount;
        private Integer status;
        private java.time.LocalDateTime createTime;
    }

    @Data
    public static class NameValue {
        private String name;
        private Number value;
    }

    @Data
    public static class MonthValue {
        private String month;
        private Number orders;
        private Number amount;
    }

    @Data
    public static class SalesTrendPoint {
        private String label;
        private Number sales;
        private Number orders;
    }
}

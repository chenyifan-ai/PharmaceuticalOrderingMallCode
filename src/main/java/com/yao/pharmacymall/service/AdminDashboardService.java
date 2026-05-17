package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.pharmacymall.dto.DashboardStatsVO;
import com.yao.pharmacymall.entity.*;
import com.yao.pharmacymall.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private ProductService productService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    public DashboardStatsVO getStats() {
        DashboardStatsVO vo = new DashboardStatsVO();

        List<Order> orders = orderService.list(
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime));
        vo.setOrderTotal(orders.size());
        long completed = orders.stream().filter(o -> OrderStatus.COMPLETED.getCode().equals(o.getStatus())).count();
        vo.setOrderCompleted((int) completed);
        BigDecimal revenue = orders.stream()
                .filter(o -> o.getPayAmount() != null && !OrderStatus.CANCELLED.getCode().equals(o.getStatus()))
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setRevenue(revenue);
        vo.setCompletionRate(orders.isEmpty() ? 0.0
                : BigDecimal.valueOf(completed * 100.0 / orders.size()).setScale(1, RoundingMode.HALF_UP).doubleValue());

        long refundCount = orders.stream()
                .filter(o -> OrderStatus.REFUNDING.getCode().equals(o.getStatus())
                        || OrderStatus.REFUNDED.getCode().equals(o.getStatus()))
                .count();
        vo.setRefundRate(orders.isEmpty() ? 0.0
                : BigDecimal.valueOf(refundCount * 100.0 / orders.size()).setScale(1, RoundingMode.HALF_UP).doubleValue());

        vo.setProductCount((int) productService.count());
        vo.setMerchantCount((int) merchantService.count());
        vo.setUserCount((int) userService.count());

        vo.setCategorySales(buildCategorySales());
        vo.setMonthlyOrders(buildMonthlyOrders(orders));
        vo.setProductRanks(buildProductRanks());
        vo.setSalesTrendWeek(buildSalesTrend(orders, 7));
        vo.setSalesTrendMonth(buildSalesTrend(orders, 30));
        vo.setSalesTrendQuarter(buildSalesTrend(orders, 90));
        vo.setRecentOrders(buildRecentOrders(orders));

        return vo;
    }

    private List<DashboardStatsVO.RecentOrderItem> buildRecentOrders(List<Order> orders) {
        Map<Long, String> firstProductName = new HashMap<>();
        List<OrderItem> items = orderItemService.list();
        for (OrderItem item : items) {
            firstProductName.putIfAbsent(item.getOrderId(), item.getProductName());
        }
        return orders.stream()
                .sorted((a, b) -> {
                    if (a.getCreateTime() == null || b.getCreateTime() == null) {
                        return 0;
                    }
                    return b.getCreateTime().compareTo(a.getCreateTime());
                })
                .limit(16)
                .map(o -> {
                    DashboardStatsVO.RecentOrderItem row = new DashboardStatsVO.RecentOrderItem();
                    row.setOrderNo(o.getOrderNo());
                    row.setReceiverName(o.getReceiverName());
                    row.setReceiverPhone(o.getReceiverPhone());
                    row.setProductName(firstProductName.getOrDefault(o.getId(), "多商品订单"));
                    row.setPayAmount(o.getPayAmount());
                    row.setStatus(o.getStatus());
                    row.setCreateTime(o.getCreateTime());
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<DashboardStatsVO.NameValue> buildCategorySales() {
        List<Product> products = productService.list();
        Map<Long, String> categoryNames = categoryService.list().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        Map<String, BigDecimal> sum = new LinkedHashMap<>();
        for (Product p : products) {
            String name = categoryNames.getOrDefault(p.getCategoryId(), "其他");
            BigDecimal salesVal = p.getWholesalePrice() != null && p.getSales() != null
                    ? p.getWholesalePrice().multiply(BigDecimal.valueOf(p.getSales()))
                    : BigDecimal.ZERO;
            sum.merge(name, salesVal, BigDecimal::add);
        }
        return sum.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(6)
                .map(e -> {
                    DashboardStatsVO.NameValue nv = new DashboardStatsVO.NameValue();
                    nv.setName(e.getKey());
                    nv.setValue(e.getValue().intValue());
                    return nv;
                })
                .collect(Collectors.toList());
    }

    private List<DashboardStatsVO.MonthValue> buildMonthlyOrders(List<Order> orders) {
        LocalDate now = LocalDate.now();
        List<DashboardStatsVO.MonthValue> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M月");
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1);
            long count = orders.stream().filter(o -> inMonth(o.getCreateTime(), monthStart, monthEnd)).count();
            BigDecimal amount = orders.stream()
                    .filter(o -> inMonth(o.getCreateTime(), monthStart, monthEnd) && o.getPayAmount() != null)
                    .map(Order::getPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            DashboardStatsVO.MonthValue mv = new DashboardStatsVO.MonthValue();
            mv.setMonth(monthStart.format(fmt));
            mv.setOrders(count);
            mv.setAmount(amount.intValue());
            list.add(mv);
        }
        return list;
    }

    private boolean inMonth(LocalDateTime time, LocalDate start, LocalDate end) {
        if (time == null) return false;
        LocalDate d = time.toLocalDate();
        return !d.isBefore(start) && d.isBefore(end);
    }

    private List<DashboardStatsVO.NameValue> buildProductRanks() {
        List<OrderItem> items = orderItemService.list();
        Map<String, Integer> qty = new HashMap<>();
        for (OrderItem item : items) {
            if (item.getProductName() == null) continue;
            qty.merge(item.getProductName(), item.getQuantity() != null ? item.getQuantity() : 0, Integer::sum);
        }
        return qty.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(8)
                .map(e -> {
                    DashboardStatsVO.NameValue nv = new DashboardStatsVO.NameValue();
                    nv.setName(e.getKey());
                    nv.setValue(e.getValue());
                    return nv;
                })
                .collect(Collectors.toList());
    }

    private List<DashboardStatsVO.SalesTrendPoint> buildSalesTrend(List<Order> orders, int days) {
        LocalDate today = LocalDate.now();
        List<DashboardStatsVO.SalesTrendPoint> list = new ArrayList<>();
        int step = days <= 7 ? 1 : (days <= 30 ? 1 : 3);
        for (int i = days - 1; i >= 0; i -= step) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            List<Order> dayOrders = orders.stream()
                    .filter(o -> o.getCreateTime() != null
                            && !o.getCreateTime().isBefore(start)
                            && o.getCreateTime().isBefore(end))
                    .collect(Collectors.toList());
            BigDecimal sales = dayOrders.stream()
                    .map(Order::getPayAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            DashboardStatsVO.SalesTrendPoint p = new DashboardStatsVO.SalesTrendPoint();
            p.setLabel(days <= 7 ? day.format(DateTimeFormatter.ofPattern("MM-dd"))
                    : day.format(DateTimeFormatter.ofPattern("M/d")));
            p.setSales(sales.intValue());
            p.setOrders(dayOrders.size());
            list.add(p);
        }
        return list;
    }
}

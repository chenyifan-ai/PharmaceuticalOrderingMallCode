package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.pharmacymall.dto.PurchaseStatsVO;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.entity.OrderItem;
import com.yao.pharmacymall.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseStatsService {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

    public PurchaseStatsVO getUserStats(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.ne(Order::getStatus, OrderStatus.CANCELLED.getCode());
        List<Order> orders = orderService.list(wrapper);

        PurchaseStatsVO vo = new PurchaseStatsVO();
        vo.setOrderCount((long) orders.size());
        BigDecimal total = orders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalPayAmount(total);
        vo.setCompletedCount(orders.stream()
                .filter(o -> OrderStatus.COMPLETED.getCode().equals(o.getStatus()))
                .count());

        if (orders.isEmpty()) {
            vo.setTopProducts(List.of());
            return vo;
        }
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));

        Map<Long, PurchaseStatsVO.ProductRankItem> rankMap = new HashMap<>();
        for (OrderItem item : items) {
            PurchaseStatsVO.ProductRankItem rank = rankMap.computeIfAbsent(item.getProductId(), id -> {
                PurchaseStatsVO.ProductRankItem r = new PurchaseStatsVO.ProductRankItem();
                r.setProductId(item.getProductId());
                r.setProductName(item.getProductName());
                r.setQuantity(0);
                r.setAmount(BigDecimal.ZERO);
                return r;
            });
            rank.setQuantity(rank.getQuantity() + item.getQuantity());
            rank.setAmount(rank.getAmount().add(
                    item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO));
        }
        List<PurchaseStatsVO.ProductRankItem> top = new ArrayList<>(rankMap.values());
        top.sort(Comparator.comparing(PurchaseStatsVO.ProductRankItem::getAmount).reversed());
        vo.setTopProducts(top.stream().limit(10).collect(Collectors.toList()));
        return vo;
    }

    public byte[] exportCsv(Long userId) {
        PurchaseStatsVO stats = getUserStats(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF");
        sb.append("指标,数值\n");
        sb.append("订单数,").append(stats.getOrderCount()).append("\n");
        sb.append("采购总额,").append(stats.getTotalPayAmount()).append("\n");
        sb.append("已完成订单,").append(stats.getCompletedCount()).append("\n\n");
        sb.append("商品名称,采购数量,采购金额\n");
        for (PurchaseStatsVO.ProductRankItem p : stats.getTopProducts()) {
            sb.append(csv(p.getProductName())).append(',')
                    .append(p.getQuantity()).append(',')
                    .append(p.getAmount()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String csv(String s) {
        if (s == null) {
            return "";
        }
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}

package com.yao.pharmacymall.service;

import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.dto.LogisticsTraceItemVO;
import com.yao.pharmacymall.entity.LogisticsInfo;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogisticsTraceService {

    private final OrderService orderService;
    private final LogisticsInfoService logisticsInfoService;

    public List<LogisticsTraceItemVO> getTraceForUser(Long userId, Long orderId) {
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        return buildTrace(order);
    }

    public List<LogisticsTraceItemVO> buildTrace(Order order) {
        List<LogisticsTraceItemVO> list = new ArrayList<>();

        add(list, order.getCreateTime(), "已下单", null, "订单已提交，等待付款");
        if (order.getPayTime() != null) {
            add(list, order.getPayTime(), "已付款", null, "支付成功");
        }
        if (order.getStatus() != null && order.getStatus() >= OrderStatus.PENDING_SHIPMENT.getCode()
                && order.getStatus() != OrderStatus.CANCELLED.getCode()) {
            add(list, order.getPayTime() != null ? order.getPayTime().plusMinutes(30) : order.getCreateTime().plusHours(1),
                    "待发货", null, "商家备货中");
        }
        if (order.getShipTime() != null) {
            String loc = StringUtils.hasText(order.getLogisticsCompany())
                    ? order.getLogisticsCompany() + " 已揽收" : "仓库";
            add(list, order.getShipTime(), "已发货", loc,
                    "运单号 " + (order.getLogisticsNo() != null ? order.getLogisticsNo() : "—"));
        }

        List<LogisticsInfo> infos = logisticsInfoService.getListByOrderId(order.getId());
        for (LogisticsInfo info : infos) {
            add(list, info.getUpdateTime(), info.getStatus(), info.getCurrentLocation(),
                    info.getStatus() + (StringUtils.hasText(info.getCurrentLocation())
                            ? " · " + info.getCurrentLocation() : ""));
        }

        if (order.getReceiveTime() != null) {
            add(list, order.getReceiveTime(), "已签收", order.getReceiverAddress(), "包裹已签收");
        } else if (order.getStatus() != null && order.getStatus().equals(OrderStatus.COMPLETED.getCode())) {
            add(list, order.getUpdateTime(), "已完成", null, "订单已完成");
        }

        if (list.isEmpty()) {
            return list;
        }
        list.sort(Comparator.comparing(LogisticsTraceItemVO::getTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    public void seedShipTrace(Order order) {
        if (order == null || order.getId() == null) {
            return;
        }
        logisticsInfoService.updateLogisticsInfo(
                order.getId(),
                order.getLogisticsCompany(),
                order.getLogisticsNo(),
                "运输中",
                "转运中心"
        );
        logisticsInfoService.updateLogisticsInfo(
                order.getId(),
                order.getLogisticsCompany(),
                order.getLogisticsNo(),
                "派送中",
                "目的地城市"
        );
    }

    private static void add(List<LogisticsTraceItemVO> list, LocalDateTime time,
                            String status, String location, String description) {
        LogisticsTraceItemVO vo = new LogisticsTraceItemVO();
        vo.setTime(time);
        vo.setStatus(status);
        vo.setLocation(location);
        vo.setDescription(description);
        list.add(vo);
    }
}

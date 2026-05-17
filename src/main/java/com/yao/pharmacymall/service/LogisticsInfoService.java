package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.pharmacymall.entity.LogisticsInfo;

import java.util.List;

public interface LogisticsInfoService extends IService<LogisticsInfo> {
    /**
     * 根据订单ID获取物流信息列表
     */
    List<LogisticsInfo> getListByOrderId(Long orderId);

    /**
     * 获取订单最新的物流信息
     */
    LogisticsInfo getLatestByOrderId(Long orderId);

    /**
     * 更新订单物流信息
     */
    Boolean updateLogisticsInfo(Long orderId, String expressCompany, String trackingNumber, String status, String location);
}
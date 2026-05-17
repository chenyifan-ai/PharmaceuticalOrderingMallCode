package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.LogisticsInfo;
import com.yao.pharmacymall.mapper.LogisticsInfoMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogisticsInfoServiceImpl extends ServiceImpl<LogisticsInfoMapper, LogisticsInfo> implements LogisticsInfoService {

    @Override
    public List<LogisticsInfo> getListByOrderId(Long orderId) {
        LambdaQueryWrapper<LogisticsInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsInfo::getOrderId, orderId)
               .orderByDesc(LogisticsInfo::getUpdateTime);
        return this.list(wrapper);
    }

    @Override
    public LogisticsInfo getLatestByOrderId(Long orderId) {
        LambdaQueryWrapper<LogisticsInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsInfo::getOrderId, orderId)
               .orderByDesc(LogisticsInfo::getUpdateTime)
               .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public Boolean updateLogisticsInfo(Long orderId, String expressCompany, String trackingNumber, String status, String location) {
        LogisticsInfo logisticsInfo = new LogisticsInfo();
        logisticsInfo.setOrderId(orderId);
        logisticsInfo.setExpressCompany(expressCompany);
        logisticsInfo.setTrackingNumber(trackingNumber);
        logisticsInfo.setStatus(status);
        logisticsInfo.setCurrentLocation(location);
        logisticsInfo.setUpdateTime(LocalDateTime.now());
        
        return this.save(logisticsInfo);
    }
}
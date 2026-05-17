package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 物流信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("logistics_info")
public class LogisticsInfo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 运单号
     */
    private String trackingNumber;

    /**
     * 物流状态
     */
    private String status;

    /**
     * 当前位置
     */
    private String currentLocation;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存变动记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_log")
public class StockLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String productName;

    /** 1-入库 2-出库 3-盘点设置 */
    private Integer changeType;

    private Integer quantityBefore;

    private Integer quantityChange;

    private Integer quantityAfter;

    private String reason;

    private Long operatorId;

    private String operatorName;

    private Long supplierId;
}

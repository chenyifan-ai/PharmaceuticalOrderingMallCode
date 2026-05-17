package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息通知实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 消息类型: 1-订单通知, 2-处方审核, 3-物流更新, 4-促销推送, 5-用药提醒, 6-系统通知
     */
    private Integer messageType;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联业务ID(如订单ID)
     */
    private Long businessId;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private java.time.LocalDateTime readTime;
}

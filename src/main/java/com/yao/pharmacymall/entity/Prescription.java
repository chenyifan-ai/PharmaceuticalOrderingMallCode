package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 处方实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prescription")
public class Prescription extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 处方编号
     */
    private String prescriptionNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用药人ID
     */
    private Long medicationUserId;

    /**
     * 订单ID（关联的订单）
     */
    private Long orderId;

    /**
     * 处方类型: 1-用户上传, 2-电子处方, 3-在线问诊
     */
    private Integer prescriptionType;

    /**
     * 诊断信息
     */
    private String diagnosis;

    /**
     * 处方图片URL列表(JSON数组)
     */
    private String imageUrls;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 医院名称
     */
    private String hospitalName;

    /**
     * 审核状态: 0-待审核, 1-通过, 2-拒绝, 3-过期
     */
    private Integer auditStatus;

    /**
     * 审核药师ID
     */
    private Long auditorId;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 处方有效期
     */
    private LocalDateTime expireTime;

    /**
     * 电子签名URL
     */
    private String signatureUrl;
}

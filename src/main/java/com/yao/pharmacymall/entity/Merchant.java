package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant")
public class Merchant extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 商家Logo
     */
    private String logo;

    /**
     * 营业执照号
     */
    private String businessLicense;

    /**
     * 营业执照图片
     */
    private String businessLicenseImage;

    /**
     * 法人姓名
     */
    private String legalPerson;

    /**
     * 法人身份证号
     */
    private String legalPersonIdCard;

    /**
     * 法人身份证正面图片
     */
    private String legalPersonIdFront;

    /**
     * 法人身份证反面图片
     */
    private String legalPersonIdBack;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 店铺地址
     */
    private String shopAddress;

    /**
     * 店铺描述
     */
    private String description;

    /**
     * 经营范围
     */
    private String businessScope;

    /**
     * 审核状态: 0-待审核, 1-已通过, 2-已拒绝
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private java.time.LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 店铺评分
     */
    private Double rating;

    /**
     * 销量统计
     */
    private Integer salesCount;

    /**
     * 保证金
     */
    private java.math.BigDecimal deposit;

    /**
     * 结算账户类型: 1-银行卡, 2-支付宝
     */
    private Integer settlementAccountType;

    /**
     * 结算账户号
     */
    private String settlementAccountNo;

    /**
     * 结算账户名
     */
    private String settlementAccountName;

    /**
     * 开户行
     */
    private String bankName;
}

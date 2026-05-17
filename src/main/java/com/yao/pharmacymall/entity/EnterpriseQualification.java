package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 企业资质信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("enterprise_qualification")
public class EnterpriseQualification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 法定代表人
     */
    private String legalPerson;

    /**
     * 法人身份证号
     */
    private String legalPersonIdCard;

    /**
     * 营业执照照片
     */
    private String businessLicenseUrl;

    /** 药品经营许可证 */
    private String drugOperationPermitUrl;

    /** 医疗器械经营许可证 */
    private String medicalDevicePermitUrl;

    /** GSP 认证证书 */
    private String gspCertificateUrl;

    /**
     * 资质审核状态：0-待审核，1-已通过，2-未通过
     */
    private Integer qualificationStatus;

    /**
     * 资质驳回原因
     */
    private String qualificationRejectReason;

    /**
     * 资质到期日期
     */
    private LocalDate qualificationExpireDate;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private java.time.LocalDateTime reviewTime;
}
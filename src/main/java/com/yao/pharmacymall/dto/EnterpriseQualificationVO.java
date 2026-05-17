package com.yao.pharmacymall.dto;

import com.yao.pharmacymall.entity.EnterpriseQualification;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业资质详情（含提交人信息，管理端展示）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnterpriseQualificationVO extends EnterpriseQualification {

    private String submitterNickname;
    private String submitterPhone;
    private String submitterRealName;
}

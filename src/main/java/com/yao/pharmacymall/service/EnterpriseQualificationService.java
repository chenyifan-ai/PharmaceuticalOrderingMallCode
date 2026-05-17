package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.dto.EnterpriseQualificationVO;
import com.yao.pharmacymall.entity.EnterpriseQualification;

public interface EnterpriseQualificationService extends IService<EnterpriseQualification> {
    /**
     * 根据用户ID获取企业资质信息
     */
    EnterpriseQualification getByUserId(Long userId);

    /**
     * 提交企业资质认证
     */
    Boolean submitQualification(Long userId, EnterpriseQualification qualification);

    /**
     * 审核企业资质
     */
    Boolean reviewQualification(Long qualificationId, Integer status, String reason, Long reviewerId);

    /**
     * 获取企业资质列表（管理员）
     */
    PageResult<EnterpriseQualification> getQualificationList(Integer status, Integer page, Integer pageSize);

    EnterpriseQualificationVO getDetailVo(Long id);
}
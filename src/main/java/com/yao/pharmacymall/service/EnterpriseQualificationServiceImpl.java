package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.dto.EnterpriseQualificationVO;
import com.yao.pharmacymall.entity.EnterpriseQualification;
import com.yao.pharmacymall.entity.User;
import com.yao.pharmacymall.enums.QualificationStatus;
import com.yao.pharmacymall.mapper.EnterpriseQualificationMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class EnterpriseQualificationServiceImpl extends ServiceImpl<EnterpriseQualificationMapper, EnterpriseQualification>
        implements EnterpriseQualificationService {

    private static final Pattern CREDIT_CODE = Pattern.compile("^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$", Pattern.CASE_INSENSITIVE);

    @Autowired
    private UserService userService;

    @Override
    public EnterpriseQualification getByUserId(Long userId) {
        LambdaQueryWrapper<EnterpriseQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseQualification::getUserId, userId);
        wrapper.orderByDesc(EnterpriseQualification::getUpdateTime);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper, false);
    }

    @Override
    public Boolean submitQualification(Long userId, EnterpriseQualification qualification) {
        validateSubmit(qualification);

        EnterpriseQualification existing = getByUserId(userId);
        if (existing != null && QualificationStatus.APPROVED.getCode().equals(existing.getQualificationStatus())) {
            throw new BusinessException("资质已通过审核，如需变更请联系管理员");
        }

        qualification.setUserId(userId);
        qualification.setQualificationStatus(QualificationStatus.PENDING.getCode());
        qualification.setQualificationRejectReason(null);
        qualification.setReviewerId(null);
        qualification.setReviewTime(null);

        if (existing != null) {
            qualification.setId(existing.getId());
            return this.updateById(qualification);
        }
        return this.save(qualification);
    }

    @Override
    public Boolean reviewQualification(Long qualificationId, Integer status, String reason, Long reviewerId) {
        EnterpriseQualification qualification = this.getById(qualificationId);
        if (qualification == null) {
            throw new BusinessException("资质记录不存在");
        }
        if (!QualificationStatus.PENDING.getCode().equals(qualification.getQualificationStatus())) {
            throw new BusinessException("仅待审核状态的资质可审核");
        }
        if (QualificationStatus.REJECTED.getCode().equals(status) && !StringUtils.hasText(reason)) {
            throw new BusinessException("驳回时必须填写原因");
        }
        if (qualification.getQualificationExpireDate() != null
                && qualification.getQualificationExpireDate().isBefore(LocalDate.now())
                && QualificationStatus.APPROVED.getCode().equals(status)) {
            throw new BusinessException("资质已过期，无法通过审核");
        }

        qualification.setQualificationStatus(status);
        qualification.setQualificationRejectReason(
                QualificationStatus.REJECTED.getCode().equals(status) ? reason : null);
        qualification.setReviewerId(reviewerId);
        qualification.setReviewTime(LocalDateTime.now());
        boolean ok = this.updateById(qualification);

        if (ok && QualificationStatus.APPROVED.getCode().equals(status)) {
            User user = userService.getById(qualification.getUserId());
            if (user != null && (user.getRealNameStatus() == null || user.getRealNameStatus() != 1)) {
                user.setRealNameStatus(1);
                if (StringUtils.hasText(qualification.getLegalPerson())) {
                    user.setRealName(qualification.getLegalPerson());
                }
                userService.updateById(user);
            }
        }
        return ok;
    }

    @Override
    public PageResult<EnterpriseQualification> getQualificationList(Integer status, Integer page, Integer pageSize) {
        Page<EnterpriseQualification> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<EnterpriseQualification> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(EnterpriseQualification::getQualificationStatus, status);
        }
        wrapper.orderByDesc(EnterpriseQualification::getCreateTime);
        IPage<EnterpriseQualification> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    public EnterpriseQualificationVO getDetailVo(Long id) {
        EnterpriseQualification q = this.getById(id);
        if (q == null) {
            throw new BusinessException("资质记录不存在");
        }
        EnterpriseQualificationVO vo = new EnterpriseQualificationVO();
        BeanUtils.copyProperties(q, vo);
        User user = userService.getById(q.getUserId());
        if (user != null) {
            vo.setSubmitterPhone(user.getPhone());
            vo.setSubmitterNickname(user.getNickname());
            vo.setSubmitterRealName(user.getRealName());
        }
        return vo;
    }

    private void validateSubmit(EnterpriseQualification q) {
        if (!StringUtils.hasText(q.getCompanyName())) {
            throw new BusinessException("请填写企业名称");
        }
        if (!StringUtils.hasText(q.getCreditCode()) || !CREDIT_CODE.matcher(q.getCreditCode().trim()).matches()) {
            throw new BusinessException("统一社会信用代码格式不正确");
        }
        if (!StringUtils.hasText(q.getLegalPerson())) {
            throw new BusinessException("请填写法定代表人");
        }
        if (!StringUtils.hasText(q.getBusinessLicenseUrl())) {
            throw new BusinessException("请上传营业执照");
        }
        if (q.getQualificationExpireDate() == null) {
            throw new BusinessException("请选择资质到期日期");
        }
        q.setCreditCode(q.getCreditCode().trim().toUpperCase());
    }
}

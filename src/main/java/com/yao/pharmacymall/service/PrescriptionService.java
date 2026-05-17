package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.Prescription;
import com.yao.pharmacymall.enums.PrescriptionStatus;
import com.yao.pharmacymall.mapper.PrescriptionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 处方服务类
 */
@Service
public class PrescriptionService extends ServiceImpl<PrescriptionMapper, Prescription> {

    /**
     * C端: 上传处方
     */
    public Prescription uploadPrescription(Long userId, Long medicationUserId, Integer prescriptionType, String imageUrl) {
        Prescription prescription = new Prescription();
        prescription.setPrescriptionNo(generatePrescriptionNo());
        prescription.setUserId(userId);
        prescription.setMedicationUserId(medicationUserId);
        prescription.setPrescriptionType(prescriptionType);
        prescription.setImageUrls(imageUrl);
        prescription.setAuditStatus(PrescriptionStatus.PENDING.getCode());
        // 设置处方有效期为3天
        prescription.setExpireTime(LocalDateTime.now().plusDays(3));

        this.save(prescription);
        return prescription;
    }

    /**
     * 生成处方编号
     */
    private String generatePrescriptionNo() {
        return "RX" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * C端: 获取用户处方列表
     */
    public PageResult<Prescription> getUserPrescriptions(Long userId, Integer page, Integer pageSize) {
        Page<Prescription> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getUserId, userId);
        wrapper.orderByDesc(Prescription::getCreateTime);

        IPage<Prescription> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * C端: 获取处方详情
     */
    public Prescription getPrescriptionDetail(Long userId, Long prescriptionId) {
        Prescription prescription = this.getById(prescriptionId);
        if (prescription == null || !prescription.getUserId().equals(userId)) {
            throw new BusinessException("处方不存在");
        }
        return prescription;
    }

    /**
     * 药师: 审核处方
     */
    public void auditPrescription(Long pharmacistId, Long prescriptionId, Integer status, String remark) {
        Prescription prescription = this.getById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }

        if (!prescription.getAuditStatus().equals(PrescriptionStatus.PENDING.getCode())) {
            throw new BusinessException("处方状态不正确");
        }

        prescription.setAuditStatus(status);
        prescription.setAuditorId(pharmacistId);
        prescription.setAuditTime(LocalDateTime.now());
        prescription.setAuditRemark(remark);

        this.updateById(prescription);

        // TODO: 发送通知给用户
    }

    /**
     * 药师: 获取待审核处方列表
     */
    public PageResult<Prescription> getPendingPrescriptions(Integer page, Integer pageSize) {
        Page<Prescription> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getAuditStatus, PrescriptionStatus.PENDING.getCode());
        wrapper.orderByAsc(Prescription::getCreateTime);

        IPage<Prescription> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * 检查处方是否有效
     */
    public boolean isPrescriptionValid(Long prescriptionId) {
        Prescription prescription = this.getById(prescriptionId);
        if (prescription == null) {
            return false;
        }

        if (!prescription.getAuditStatus().equals(PrescriptionStatus.APPROVED.getCode())) {
            return false;
        }

        if (prescription.getExpireTime() != null && LocalDateTime.now().isAfter(prescription.getExpireTime())) {
            prescription.setAuditStatus(PrescriptionStatus.EXPIRED.getCode());
            this.updateById(prescription);
            return false;
        }

        return true;
    }

    /**
     * 药师: 获取审核历史
     */
    public PageResult<Prescription> getAuditHistory(Long pharmacistId, Integer page, Integer pageSize) {
        Page<Prescription> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getAuditorId, pharmacistId);
        wrapper.in(Prescription::getAuditStatus, PrescriptionStatus.APPROVED.getCode(), PrescriptionStatus.REJECTED.getCode());
        wrapper.orderByDesc(Prescription::getAuditTime);

        IPage<Prescription> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * 药师: 获取审核统计
     */
    public Map<String, Object> getPharmacistStatistics(Long pharmacistId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<Prescription> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Prescription::getAuditorId, pharmacistId);
        long total = this.count(totalWrapper);

        LambdaQueryWrapper<Prescription> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Prescription::getAuditorId, pharmacistId)
                       .eq(Prescription::getAuditStatus, PrescriptionStatus.APPROVED.getCode());
        long approved = this.count(approvedWrapper);

        LambdaQueryWrapper<Prescription> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(Prescription::getAuditorId, pharmacistId)
                       .eq(Prescription::getAuditStatus, PrescriptionStatus.REJECTED.getCode());
        long rejected = this.count(rejectedWrapper);

        LambdaQueryWrapper<Prescription> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Prescription::getAuditStatus, PrescriptionStatus.PENDING.getCode());
        long pending = this.count(pendingWrapper);

        stats.put("total", total);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("pending", pending);
        stats.put("approvalRate", total > 0 ? (double) approved / total * 100 : 0);

        return stats;
    }

    /**
     * 管理员: 获取处方列表
     */
    public PageResult<Prescription> getAdminPrescriptionList(Integer status, Integer page, Integer pageSize) {
        Page<Prescription> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Prescription::getAuditStatus, status);
        }

        wrapper.orderByDesc(Prescription::getCreateTime);

        IPage<Prescription> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * 管理员: 获取处方详情
     */
    public Prescription getAdminPrescriptionDetail(Long prescriptionId) {
        Prescription prescription = this.getById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        return prescription;
    }
}

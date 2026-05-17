package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.Merchant;
import com.yao.pharmacymall.mapper.MerchantMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家服务类
 */
@Service
public class MerchantService extends ServiceImpl<MerchantMapper, Merchant> {

    /**
     * 管理员: 获取商家列表
     */
    public PageResult<Merchant> getMerchantList(Integer auditStatus, String keyword, Integer page, Integer pageSize) {
        Page<Merchant> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();

        if (auditStatus != null) {
            wrapper.eq(Merchant::getAuditStatus, auditStatus);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Merchant::getMerchantName, keyword)
                    .or()
                    .like(Merchant::getLegalPerson, keyword)
                    .or()
                    .like(Merchant::getContactPhone, keyword));
        }

        wrapper.orderByDesc(Merchant::getCreateTime);

        IPage<Merchant> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * 管理员: 审核商家
     */
    public void auditMerchant(Long auditorId, Long merchantId, Integer auditStatus, String auditRemark) {
        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }

        if (merchant.getAuditStatus() != null && merchant.getAuditStatus() != 0) {
            throw new BusinessException("该商家已审核");
        }

        merchant.setAuditStatus(auditStatus);
        merchant.setAuditRemark(auditRemark);
        merchant.setAuditorId(auditorId);
        merchant.setAuditTime(LocalDateTime.now());
        this.updateById(merchant);
    }

    /**
     * 根据用户ID获取商家信息
     */
    public Merchant getMerchantByUserId(Long userId) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getUserId, userId);
        return this.getOne(wrapper);
    }

    /**
     * B端: 商家更新自己的信息
     */
    public void updateMerchantInfo(Long userId, Merchant merchant) {
        Merchant existingMerchant = getMerchantByUserId(userId);
        if (existingMerchant == null) {
            throw new BusinessException("商家信息不存在");
        }

        merchant.setId(existingMerchant.getId());
        merchant.setUserId(userId);
        // 需要重新审核
        merchant.setAuditStatus(0);
        merchant.setAuditRemark(null);
        merchant.setAuditorId(null);
        merchant.setAuditTime(null);
        this.updateById(merchant);
    }

    /**
     * 管理员新增供应商
     */
    public Merchant adminCreateMerchant(Merchant merchant) {
        if (!StringUtils.hasText(merchant.getMerchantName())) {
            throw new BusinessException("供应商名称不能为空");
        }
        if (merchant.getUserId() == null) {
            throw new BusinessException("请关联用户账号");
        }
        if (getMerchantByUserId(merchant.getUserId()) != null) {
            throw new BusinessException("该用户已绑定供应商");
        }
        if (merchant.getAuditStatus() == null) {
            merchant.setAuditStatus(0);
        }
        if (merchant.getRating() == null) {
            merchant.setRating(5.0);
        }
        if (merchant.getSalesCount() == null) {
            merchant.setSalesCount(0);
        }
        if (merchant.getDeposit() == null) {
            merchant.setDeposit(BigDecimal.ZERO);
        }
        if (merchant.getSettlementAccountType() == null) {
            merchant.setSettlementAccountType(1);
        }
        this.save(merchant);
        return merchant;
    }

    /**
     * 管理员更新供应商
     */
    public void adminUpdateMerchant(Merchant merchant) {
        Merchant exist = this.getById(merchant.getId());
        if (exist == null) {
            throw new BusinessException("供应商不存在");
        }
        merchant.setUserId(exist.getUserId());
        this.updateById(merchant);
    }

    /**
     * 管理员删除供应商
     */
    public void adminDeleteMerchant(Long id) {
        if (this.getById(id) == null) {
            throw new BusinessException("供应商不存在");
        }
        this.removeById(id);
    }
}

package com.yao.pharmacymall.service;

import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.EnterpriseQualification;
import com.yao.pharmacymall.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 采购资格与数量校验（资质门禁、起订量、限购）
 */
@Service
public class PurchaseGuardService {

    @Autowired
    private EnterpriseQualificationService enterpriseQualificationService;

    public void requireApprovedQualification(Long userId) {
        EnterpriseQualification q = enterpriseQualificationService.getByUserId(userId);
        if (q == null || q.getQualificationStatus() == null || q.getQualificationStatus() != 1) {
            throw new BusinessException("请先完成企业资质认证并通过审核后再采购");
        }
        if (q.getQualificationExpireDate() != null
                && q.getQualificationExpireDate().isBefore(LocalDate.now())) {
            throw new BusinessException("企业资质已过期，请更新资质后重新提交审核");
        }
    }

    public void validateQuantity(Product product, int quantity) {
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        int min = product.getMinOrderQuantity() != null && product.getMinOrderQuantity() > 0
                ? product.getMinOrderQuantity() : 1;
        if (quantity < min) {
            throw new BusinessException(String.format("%s 起订量为 %d 件", product.getProductName(), min));
        }
        if (product.getMaxOrderQuantity() != null
                && product.getMaxOrderQuantity() > 0
                && quantity > product.getMaxOrderQuantity()) {
            throw new BusinessException(String.format(
                    "%s 单次限购 %d 件", product.getProductName(), product.getMaxOrderQuantity()));
        }
    }

    public void requireOtcProduct(Product product) {
        if (product != null && "PRESCRIPTION".equalsIgnoreCase(product.getPrescriptionType())) {
            throw new BusinessException("处方药请使用处方下单流程：" + product.getProductName());
        }
    }
}

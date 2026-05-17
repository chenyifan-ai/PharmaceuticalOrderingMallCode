package com.yao.pharmacymall.service;

import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.dto.UserCouponVO;
import com.yao.pharmacymall.entity.Coupon;
import com.yao.pharmacymall.entity.UserCoupon;
import com.yao.pharmacymall.enums.CouponStatus;
import com.yao.pharmacymall.enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponCheckoutService {

    private final UserCouponService userCouponService;
    private final CouponService couponService;

    public BigDecimal calculateDiscount(Long userCouponId, Long userId, BigDecimal orderAmount) {
        if (userCouponId == null) {
            return BigDecimal.ZERO;
        }
        UserCoupon uc = userCouponService.getById(userCouponId);
        if (uc == null || !uc.getUserId().equals(userId)) {
            throw new BusinessException("优惠券不存在");
        }
        if (!CouponStatus.UNUSED.getCode().equals(uc.getStatus())) {
            throw new BusinessException("优惠券不可用");
        }
        Coupon coupon = couponService.getById(uc.getCouponId());
        if (coupon == null || coupon.getStatus() == null
                || !coupon.getStatus().equals(StatusEnum.ENABLE.getCode())) {
            throw new BusinessException("优惠券已失效");
        }
        LocalDate today = LocalDate.now();
        if (coupon.getValidStartDate() != null && today.isBefore(coupon.getValidStartDate())) {
            throw new BusinessException("优惠券未到使用时间");
        }
        if (coupon.getValidEndDate() != null && today.isAfter(coupon.getValidEndDate())) {
            throw new BusinessException("优惠券已过期");
        }
        if (coupon.getMinOrderAmount() != null
                && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BusinessException("未达到优惠券使用门槛");
        }
        BigDecimal discount;
        if ("CASH".equalsIgnoreCase(coupon.getType())) {
            discount = coupon.getDiscountValue();
        } else if ("DISCOUNT".equalsIgnoreCase(coupon.getType())) {
            BigDecimal rate = coupon.getDiscountValue();
            if (rate.compareTo(BigDecimal.ONE) > 0) {
                rate = rate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            }
            discount = orderAmount.multiply(BigDecimal.ONE.subtract(rate));
        } else {
            discount = coupon.getDiscountValue();
        }
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (discount.compareTo(orderAmount) > 0) {
            return orderAmount;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    public List<UserCouponVO> listUsable(Long userId, BigDecimal orderAmount) {
        List<UserCoupon> list = userCouponService.getListByUserIdAndStatus(userId, CouponStatus.UNUSED.getCode());
        List<UserCouponVO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (UserCoupon uc : list) {
            Coupon c = couponService.getById(uc.getCouponId());
            if (c == null || c.getStatus() == null
                    || !c.getStatus().equals(StatusEnum.ENABLE.getCode())) {
                continue;
            }
            if (c.getValidEndDate() != null && today.isAfter(c.getValidEndDate())) {
                continue;
            }
            if (c.getMinOrderAmount() != null && orderAmount != null
                    && orderAmount.compareTo(c.getMinOrderAmount()) < 0) {
                continue;
            }
            UserCouponVO vo = new UserCouponVO();
            vo.setId(uc.getId());
            vo.setCouponId(c.getId());
            vo.setName(c.getName());
            vo.setType(c.getType());
            vo.setDiscountValue(c.getDiscountValue());
            vo.setMinOrderAmount(c.getMinOrderAmount());
            vo.setValidEndDate(c.getValidEndDate());
            vo.setStatus(uc.getStatus());
            result.add(vo);
        }
        return result;
    }
}

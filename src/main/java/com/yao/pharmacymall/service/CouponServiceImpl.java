package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.Coupon;
import com.yao.pharmacymall.enums.StatusEnum;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CouponServiceImpl extends ServiceImpl<com.yao.pharmacymall.mapper.CouponMapper, Coupon> implements CouponService {

    @Override
    public List<Coupon> getValidCoupons() {
        LocalDate now = LocalDate.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getStatus, StatusEnum.ENABLE.getCode())
               .le(Coupon::getValidStartDate, now)
               .ge(Coupon::getValidEndDate, now);
        return this.list(wrapper);
    }

    @Override
    public List<Coupon> getAvailableCoupons(Long userId, BigDecimal orderAmount) {
        LocalDate now = LocalDate.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getStatus, StatusEnum.ENABLE.getCode())
               .le(Coupon::getValidStartDate, now)
               .ge(Coupon::getValidEndDate, now)
               .and(qw -> qw.le(Coupon::getMinOrderAmount, orderAmount).or().isNull(Coupon::getMinOrderAmount));
        return this.list(wrapper);
    }

    @Override
    public Boolean isCouponAvailable(Long couponId, Long userId, BigDecimal orderAmount) {
        Coupon coupon = this.getById(couponId);
        if (coupon == null) {
            return false;
        }

        LocalDate now = LocalDate.now();
        boolean isValid = coupon.getStatus().equals(StatusEnum.ENABLE.getCode()) &&
                         !now.isBefore(coupon.getValidStartDate()) &&
                         !now.isAfter(coupon.getValidEndDate());

        boolean meetsMinAmount = coupon.getMinOrderAmount() == null || 
                                 orderAmount.compareTo(coupon.getMinOrderAmount()) >= 0;

        return isValid && meetsMinAmount;
    }
}
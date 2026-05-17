package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.pharmacymall.entity.Coupon;

import java.util.List;

public interface CouponService extends IService<Coupon> {
    /**
     * 获取有效的优惠券列表
     */
    List<Coupon> getValidCoupons();

    /**
     * 根据用户和订单金额获取可用优惠券
     */
    List<Coupon> getAvailableCoupons(Long userId, java.math.BigDecimal orderAmount);

    /**
     * 检查优惠券是否可用
     */
    Boolean isCouponAvailable(Long couponId, Long userId, java.math.BigDecimal orderAmount);
}
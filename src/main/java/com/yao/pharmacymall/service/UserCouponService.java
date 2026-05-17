package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.pharmacymall.entity.UserCoupon;

import java.util.List;

public interface UserCouponService extends IService<UserCoupon> {
    /**
     * 根据用户ID获取优惠券列表
     */
    List<UserCoupon> getListByUserId(Long userId);

    /**
     * 根据用户ID和状态获取优惠券列表
     */
    List<UserCoupon> getListByUserIdAndStatus(Long userId, Integer status);

    /**
     * 用户领取优惠券
     */
    Boolean receiveCoupon(Long userId, Long couponId);

    /**
     * 使用优惠券
     */
    Boolean useCoupon(Long userCouponId);

    /**
     * 检查优惠券是否可用
     */
    Boolean isUserCouponAvailable(Long userCouponId, Long userId);
}
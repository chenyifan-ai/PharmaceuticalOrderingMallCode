package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.UserCoupon;
import com.yao.pharmacymall.enums.CouponStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCouponServiceImpl extends ServiceImpl<com.yao.pharmacymall.mapper.UserCouponMapper, UserCoupon> implements UserCouponService {

    @Override
    public List<UserCoupon> getListByUserId(Long userId) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        return this.list(wrapper);
    }

    @Override
    public List<UserCoupon> getListByUserIdAndStatus(Long userId, Integer status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
               .eq(UserCoupon::getStatus, status);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public Boolean receiveCoupon(Long userId, Long couponId) {
        // 检查是否已经领取过
        LambdaQueryWrapper<UserCoupon> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserCoupon::getUserId, userId)
                   .eq(UserCoupon::getCouponId, couponId);
        if (this.count(checkWrapper) > 0) {
            return false; // 已经领取过
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(CouponStatus.UNUSED.getCode());
        return this.save(userCoupon);
    }

    @Override
    @Transactional
    public Boolean useCoupon(Long userCouponId) {
        UserCoupon userCoupon = this.getById(userCouponId);
        if (userCoupon == null || !userCoupon.getStatus().equals(CouponStatus.UNUSED.getCode())) {
            return false;
        }

        LambdaUpdateWrapper<UserCoupon> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserCoupon::getId, userCouponId)
                     .set(UserCoupon::getStatus, CouponStatus.USED.getCode())
                     .set(UserCoupon::getUsedTime, LocalDateTime.now());
        return this.update(updateWrapper);
    }

    @Override
    public Boolean isUserCouponAvailable(Long userCouponId, Long userId) {
        UserCoupon userCoupon = this.getById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            return false;
        }

        return userCoupon.getStatus().equals(CouponStatus.UNUSED.getCode());
    }
}
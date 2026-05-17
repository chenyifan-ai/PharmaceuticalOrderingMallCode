package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.UserCouponVO;
import com.yao.pharmacymall.entity.Coupon;
import com.yao.pharmacymall.service.CouponCheckoutService;
import com.yao.pharmacymall.service.CouponService;
import com.yao.pharmacymall.service.UserCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/c/coupon")
public class CCouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private CouponCheckoutService couponCheckoutService;

    @GetMapping("/available")
    public Result<List<Coupon>> available() {
        return Result.success(couponService.getValidCoupons());
    }

    @PostMapping("/receive/{couponId}")
    public Result<?> receive(HttpServletRequest request, @PathVariable Long couponId) {
        Long userId = (Long) request.getAttribute("userId");
        if (!userCouponService.receiveCoupon(userId, couponId)) {
            return Result.error("领取失败，可能已领取或已抢光");
        }
        return Result.success("领取成功");
    }

    @GetMapping("/my")
    public Result<List<UserCouponVO>> myCoupons(
            HttpServletRequest request,
            @RequestParam(required = false) BigDecimal orderAmount) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal amount = orderAmount != null ? orderAmount : BigDecimal.ZERO;
        return Result.success(couponCheckoutService.listUsable(userId, amount));
    }
}

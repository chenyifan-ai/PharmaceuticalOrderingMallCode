package com.yao.pharmacymall.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * OTC订单创建请求
 */
@Data
public class OtcOrderRequest {

    @NotEmpty(message = "请选择购物车商品")
    private List<Long> cartItemIds;

    @NotNull(message = "请选择收货地址")
    private Long addressId;

    private Long medicationUserId;

    private String remark;

    /** 用户优惠券 ID（可选） */
    private Long userCouponId;
}

package com.yao.pharmacymall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserCouponVO {
    private Long id;
    private Long couponId;
    private String name;
    private String type;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private LocalDate validEndDate;
    private Integer status;
}

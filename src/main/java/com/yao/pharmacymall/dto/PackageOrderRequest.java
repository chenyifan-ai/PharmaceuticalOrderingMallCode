package com.yao.pharmacymall.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PackageOrderRequest {

    @NotNull(message = "套餐ID不能为空")
    private Long packageId;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    private Long medicationUserId;

    private String remark;
}

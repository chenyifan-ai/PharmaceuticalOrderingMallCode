package com.yao.pharmacymall.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 处方药订单创建请求DTO
 */
@Data
public class PrescriptionOrderRequest {

    /**
     * 处方ID（必填）
     */
    @NotNull(message = "处方ID不能为空")
    private Long prescriptionId;

    /**
     * 收货地址ID（必填）
     */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /**
     * 用药人ID（可选）
     */
    private Long medicationUserId;

    /**
     * 订单项列表（商品ID和数量）
     */
    @NotNull(message = "商品信息不能为空")
    private List<OrderItemDTO> items;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 税号
     */
    private String invoiceTaxNo;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 订单项DTO
     */
    @Data
    public static class OrderItemDTO {
        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /**
         * 购买数量
         */
        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}

package com.yao.pharmacymall.dto;

import com.yao.pharmacymall.entity.MerchantSettlement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantSettlementVO extends MerchantSettlement {
    private String merchantName;
}

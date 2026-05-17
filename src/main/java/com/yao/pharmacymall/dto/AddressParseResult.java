package com.yao.pharmacymall.dto;

import lombok.Data;

@Data
public class AddressParseResult {
    private String name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
}

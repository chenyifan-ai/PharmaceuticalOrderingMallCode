package com.yao.pharmacymall.dto;

import com.yao.pharmacymall.entity.HomeBanner;
import lombok.Data;

import java.util.List;

@Data
public class CHomeVO {
    private List<HomeBanner> banners;
    private List<SeckillItemVO> seckills;
    private List<ProductPackageVO> packages;
}

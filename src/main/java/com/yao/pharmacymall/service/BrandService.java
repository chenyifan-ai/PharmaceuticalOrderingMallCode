package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.pharmacymall.entity.Brand;

import java.util.List;

public interface BrandService extends IService<Brand> {
    /**
     * 获取所有启用的品牌
     */
    List<Brand> getAllActiveBrands();

    /**
     * 根据状态获取品牌列表
     */
    List<Brand> getListByStatus(Integer status);
}

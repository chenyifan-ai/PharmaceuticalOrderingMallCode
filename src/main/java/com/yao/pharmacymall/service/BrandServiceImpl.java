package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.Brand;
import com.yao.pharmacymall.enums.StatusEnum;
import com.yao.pharmacymall.mapper.BrandMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Override
    public List<Brand> getAllActiveBrands() {
        return getListByStatus(StatusEnum.ENABLE.getCode());
    }

    @Override
    public List<Brand> getListByStatus(Integer status) {
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getStatus, status);
        return this.list(wrapper);
    }
}

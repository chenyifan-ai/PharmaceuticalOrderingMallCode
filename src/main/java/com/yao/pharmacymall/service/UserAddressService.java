package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.UserAddress;
import com.yao.pharmacymall.mapper.UserAddressMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户地址服务类
 */
@Service
public class UserAddressService extends ServiceImpl<UserAddressMapper, UserAddress> {

    /**
     * 获取用户地址列表
     */
    public List<UserAddress> getUserAddresses(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        wrapper.orderByDesc(UserAddress::getIsDefault, UserAddress::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 获取用户默认收货地址
     */
    public UserAddress getDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        wrapper.eq(UserAddress::getIsDefault, 1);
        wrapper.last("LIMIT 1");
        UserAddress def = this.getOne(wrapper);
        if (def != null) {
            return def;
        }
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        wrapper.orderByDesc(UserAddress::getCreateTime);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 添加地址
     */
    public void addAddress(UserAddress address, Long userId) {
        address.setUserId(userId);

        // 如果是默认地址，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            setAllNonDefault(userId);
        }

        this.save(address);
    }

    /**
     * 更新地址
     */
    public void updateAddress(UserAddress address, Long userId) {
        UserAddress existAddress = this.getById(address.getId());
        if (existAddress == null || !existAddress.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }

        // 如果设置为默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            setAllNonDefault(userId);
        }

        this.updateById(address);
    }

    /**
     * 删除地址
     */
    public void deleteAddress(Long addressId, Long userId) {
        UserAddress address = this.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }

        this.removeById(addressId);
    }

    /**
     * 设置默认地址
     */
    public void setDefaultAddress(Long addressId, Long userId) {
        UserAddress address = this.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }

        setAllNonDefault(userId);
        address.setIsDefault(1);
        this.updateById(address);
    }

    /**
     * 将用户所有地址设为非默认
     */
    private void setAllNonDefault(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        List<UserAddress> addresses = this.list(wrapper);
        for (UserAddress address : addresses) {
            address.setIsDefault(0);
        }
        this.updateBatchById(addresses);
    }
}

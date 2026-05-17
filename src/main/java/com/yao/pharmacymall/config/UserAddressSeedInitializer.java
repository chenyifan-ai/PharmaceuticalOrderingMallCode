package com.yao.pharmacymall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.pharmacymall.entity.User;
import com.yao.pharmacymall.entity.UserAddress;
import com.yao.pharmacymall.service.UserAddressService;
import com.yao.pharmacymall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 为演示采购账号初始化默认收货地址
 */
@Slf4j
@Component
@Order(4)
public class UserAddressSeedInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;
    @Autowired
    private UserAddressService userAddressService;

    @Override
    public void run(String... args) {
        seedDefaultForPhone("13800000002", "张采购", "广东省", "广州市", "天河区",
                "珠江新城花城大道88号医药采购中心");
        seedDefaultForPhone("13800000000", "系统管理员", "上海市", "上海市", "浦东新区",
                "张江高科技园区科苑路100号");
    }

    private void seedDefaultForPhone(String phone, String defaultName, String province,
                                   String city, String district, String detail) {
        User user = getUserByPhone(phone);
        if (user == null) {
            return;
        }
        long count = userAddressService.lambdaQuery()
                .eq(UserAddress::getUserId, user.getId())
                .count();
        if (count > 0) {
            return;
        }
        UserAddress address = new UserAddress();
        address.setUserId(user.getId());
        address.setReceiverName(user.getRealName() != null ? user.getRealName() : defaultName);
        address.setReceiverPhone(user.getPhone());
        address.setProvince(province);
        address.setCity(city);
        address.setDistrict(district);
        address.setDetailAddress(detail);
        address.setIsDefault(1);
        userAddressService.save(address);
        log.info("已为用户 {} 创建默认收货地址", phone);
    }

    private User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.eq(User::getPhone, phone);
        return userService.getOne(w);
    }
}

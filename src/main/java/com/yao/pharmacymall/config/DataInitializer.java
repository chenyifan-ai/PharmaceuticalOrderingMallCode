package com.yao.pharmacymall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.pharmacymall.entity.User;
import com.yao.pharmacymall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 - 创建默认测试账号
 */
@Slf4j
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化测试账号...");

        // 先删除旧的测试账号（确保使用正确的密码）
        deleteTestUserIfExists("13800000000");
        deleteTestUserIfExists("13800000001");
        deleteTestUserIfExists("13800000002");

        // 创建管理员账号
        createTestUser("13800000000", "admin123", 5, "管理员", "系统管理员");

        // 创建商家账号
        createTestUser("13800000001", "admin123", 4, "测试商家", "商家用户");

        // 创建普通用户
        createTestUser("13800000002", "admin123", 1, "测试用户", "普通用户");

        log.info("测试账号初始化完成！");
        log.info("========================================");
        log.info("测试账号列表：");
        log.info("管理员: 13800000000 / admin123");
        log.info("商  家: 13800000001 / admin123");
        log.info("用  户: 13800000002 / admin123");
        log.info("========================================");
    }

    private void deleteTestUserIfExists(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        // 使用物理删除（绕过逻辑删除）
        userService.getBaseMapper().delete(wrapper);
    }

    private void createTestUser(String phone, String password, int userType, String nickname, String realName) {
        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserType(userType);
        user.setNickname(nickname);
        user.setRealName(realName);
        user.setStatus(0); // 正常状态
        user.setRealNameStatus(1); // 已实名认证

        userService.save(user);
        log.info("创建测试账号: {} ({})", phone, nickname);
    }
}

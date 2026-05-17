package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.config.JwtUtil;
import com.yao.pharmacymall.dto.LoginRequest;
import com.yao.pharmacymall.dto.LoginResponse;
import com.yao.pharmacymall.dto.RegisterRequest;
import com.yao.pharmacymall.entity.User;
import com.yao.pharmacymall.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务类（支持Redis和本地缓存）
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private SmsService smsService;

    // 本地验证码缓存（开发环境备用）
    private final Map<String, SmsCode> smsCodeCache = new ConcurrentHashMap<>();

    /**
     * 用户注册
     */
    public void register(RegisterRequest request) {
        // 验证验证码
        String cacheKey = "sms:" + request.getPhone();
        SmsCode cached = smsCodeCache.get(cacheKey);
        if (cached == null || !cached.getCode().equals(request.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        if (cached.isExpired()) {
            smsCodeCache.remove(cacheKey);
            throw new BusinessException("验证码已过期");
        }

        // 检查手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, request.getPhone());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("该手机号已注册");
        }

        // 创建用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(request.getUserType());
        user.setNickname("用户" + request.getPhone().substring(7));
        user.setStatus(0);
        user.setRealNameStatus(0);

        this.save(user);

        // 删除验证码
        smsCodeCache.remove(cacheKey);
    }

    /**
     * 用户登录
     * 支持 username（手机号或用户名）和 password，也支持 phone 字段
     */
    public LoginResponse login(LoginRequest request) {
        String username = request.getActualUsername();

        // 先按手机号查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, username);
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查账号状态
        if (user.getStatus() == 1) {
            throw new BusinessException("账号已被冻结");
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUserType());

        // 构建响应
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(user, response);
        response.setToken(token);

        return response;
    }

    /**
     * 发送短信验证码（优先使用SmsService，降级到本地缓存）
     */
    public void sendSmsCode(String phone) {
        if (smsService != null) {
            // 使用SmsService（支持Redis和真实SMS）
            smsService.sendVerificationCode(phone);
        } else {
            // 降级到本地缓存（开发环境备用）
            sendSmsCodeLocal(phone);
        }
    }

    /**
     * 本地发送验证码（备用方案）
     */
    private void sendSmsCodeLocal(String phone) {
        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }

        // 生成6位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 存储到本地缓存，有效期5分钟
        String cacheKey = "sms:" + phone;
        smsCodeCache.put(cacheKey, new SmsCode(code, System.currentTimeMillis() + 5 * 60 * 1000));

        // 开发环境直接打印验证码到控制台
        System.out.println("=========================================");
        System.out.println("【验证码】手机号: " + phone + "，验证码: " + code);
        System.out.println("=========================================");
    }

    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(Long userId, User user) {
        User existUser = this.getById(userId);
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        user.setId(userId);
        user.setPassword(null);
        this.updateById(user);
    }

    /**
     * 更新用户资料（C端采购方）
     */
    public void updateUserProfile(Long userId, User user) {
        User existUser = this.getById(userId);
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        user.setId(userId);
        user.setPassword(null);
        user.setUserType(null);
        user.setStatus(null);
        this.updateById(user);
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    /**
     * 通过手机号重置密码
     */
    public void resetPassword(String phone, String code, String newPassword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new BusinessException("手机号未注册");
        }

        String cacheKey = "sms:" + phone;
        SmsCode cached = smsCodeCache.get(cacheKey);
        if (cached == null || !cached.getCode().equals(code)) {
            throw new BusinessException("验证码错误或已过期");
        }
        if (cached.isExpired()) {
            smsCodeCache.remove(cacheKey);
            throw new BusinessException("验证码已过期");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        smsCodeCache.remove(cacheKey);
    }

    /**
     * 实名认证
     */
    public void realNameAuth(Long userId, String realName, String idCard) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setRealName(realName);
        user.setIdCard(idCard);
        user.setRealNameStatus(1);
        this.updateById(user);
    }

    /**
     * 验证码内部类
     */
    private static class SmsCode {
        private final String code;
        private final long expireTime;

        public SmsCode(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        public String getCode() {
            return code;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    // ==================== 管理员用户管理方法 ====================

    /**
     * 管理员获取用户列表（分页）
     */
    public PageResult<User> getAdminUserList(String keyword, Integer userType, Integer status, Integer page, Integer pageSize) {
        Page<User> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = this.page(pageInfo, wrapper);
        // 隐藏密码
        result.getRecords().forEach(user -> user.setPassword(null));
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * 管理员创建用户
     */
    public void createUser(User user) {
        // 检查手机号是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, user.getPhone());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("该手机号已存在");
        }

        // 默认密码 123456
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode("123456"));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getStatus() == null) {
            user.setStatus(0);
        }
        if (user.getRealNameStatus() == null) {
            user.setRealNameStatus(0);
        }

        this.save(user);
    }

    /**
     * 管理员更新用户
     */
    public void updateAdminUser(Long id, User user) {
        User existUser = this.getById(id);
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        user.setId(id);
        // 不允许通过此接口修改密码
        user.setPassword(null);
        this.updateById(user);
    }

    /**
     * 管理员删除用户（软删除）
     */
    public void deleteUser(Long id) {
        User existUser = this.getById(id);
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        this.removeById(id);
    }

    /**
     * 冻结/解冻用户
     */
    public void toggleUserStatus(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(user.getStatus() == 0 ? 1 : 0);
        this.updateById(user);
    }
}

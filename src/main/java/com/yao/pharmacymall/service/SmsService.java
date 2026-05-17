package com.yao.pharmacymall.service;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务
 * 开发环境：使用内存存储验证码并打印到日志
 * 生产环境：可对接阿里云SMS或腾讯云SMS
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "sms.enabled", havingValue = "true", matchIfMissing = true)
public class SmsService {

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /**
     * 验证码有效期（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 发送频率限制（秒）
     */
    private static final int SEND_INTERVAL_SECONDS = 60;

    /**
     * Redis Key前缀
     */
    private static final String CODE_KEY_PREFIX = "sms:code:";
    private static final String SEND_TIME_KEY_PREFIX = "sms:send_time:";

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 验证码（仅用于测试，生产环境应返回void）
     */
    public String sendVerificationCode(String phone) {
        // 验证手机号格式
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确");
        }

        // 检查发送频率
        checkSendFrequency(phone);

        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);

        // 存储验证码
        storeCode(phone, code);

        // 记录发送时间
        recordSendTime(phone);

        // 开发环境：打印到日志
        log.info("【短信验证码】手机号: {}, 验证码: {}", phone, code);

        // TODO: 生产环境对接真实SMS服务
        // sendRealSms(phone, code);

        return code;
    }

    /**
     * 验证验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否验证成功
     */
    public boolean verifyCode(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }

        String storedCode = getCode(phone);
        if (storedCode == null) {
            log.warn("验证码不存在或已过期: {}", phone);
            return false;
        }

        boolean matches = storedCode.equals(code);
        if (matches) {
            // 验证成功后删除验证码
            deleteCode(phone);
            log.info("验证码验证成功: {}", phone);
        } else {
            log.warn("验证码错误: {}", phone);
        }

        return matches;
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return false;
        }
        // 简单验证：以1开头的11位数字
        return phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 检查发送频率
     */
    private void checkSendFrequency(String phone) {
        if (redisTemplate == null) {
            // 无Redis时跳过频率检查
            return;
        }

        String key = SEND_TIME_KEY_PREFIX + phone;
        String lastSendTime = redisTemplate.opsForValue().get(key);

        if (lastSendTime != null) {
            long lastTime = Long.parseLong(lastSendTime);
            long currentTime = System.currentTimeMillis();
            long elapsed = (currentTime - lastTime) / 1000;

            if (elapsed < SEND_INTERVAL_SECONDS) {
                throw new IllegalStateException(
                    String.format("发送过于频繁，请%d秒后再试", SEND_INTERVAL_SECONDS - elapsed)
                );
            }
        }
    }

    /**
     * 存储验证码
     */
    private void storeCode(String phone, String code) {
        if (redisTemplate != null) {
            // 使用Redis存储
            String key = CODE_KEY_PREFIX + phone;
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } else {
            // 无Redis时使用内存存储（仅用于开发测试）
            log.warn("Redis未启用，验证码存储在内存中（重启后失效）");
            // 实际项目中应使用ConcurrentHashMap或其他持久化方式
        }
    }

    /**
     * 获取验证码
     */
    private String getCode(String phone) {
        if (redisTemplate != null) {
            String key = CODE_KEY_PREFIX + phone;
            return redisTemplate.opsForValue().get(key);
        }
        return null;
    }

    /**
     * 删除验证码
     */
    private void deleteCode(String phone) {
        if (redisTemplate != null) {
            String key = CODE_KEY_PREFIX + phone;
            redisTemplate.delete(key);
        }
    }

    /**
     * 记录发送时间
     */
    private void recordSendTime(String phone) {
        if (redisTemplate != null) {
            String key = SEND_TIME_KEY_PREFIX + phone;
            redisTemplate.opsForValue().set(
                key,
                String.valueOf(System.currentTimeMillis()),
                SEND_INTERVAL_SECONDS,
                TimeUnit.SECONDS
            );
        }
    }

    /**
     * 发送真实短信（生产环境实现）
     *
     * @param phone 手机号
     * @param code  验证码
     */
    private void sendRealSms(String phone, String code) {
        // TODO: 对接阿里云SMS
        // DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        // IAcsClient client = new DefaultAcsClient(profile);
        // CommonRequest request = new CommonRequest();
        // request.setSysMethod(MethodType.POST);
        // request.setSysDomain("dysmsapi.aliyuncs.com");
        // request.setSysVersion("2017-05-25");
        // request.setSysAction("SendSms");
        // request.putQueryParameter("RegionId", "cn-hangzhou");
        // request.putQueryParameter("PhoneNumbers", phone);
        // request.putQueryParameter("SignName", "医药商城");
        // request.putQueryParameter("TemplateCode", "SMS_123456789");
        // request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");
        // CommonResponse response = client.getCommonResponse(request);

        // TODO: 对接腾讯云SMS
        // Credential cred = new Credential(secretId, secretKey);
        // HttpProfile httpProfile = new HttpProfile();
        // httpProfile.setEndpoint("sms.tencentcloudapi.com");
        // ClientProfile clientProfile = new ClientProfile();
        // clientProfile.setHttpProfile(httpProfile);
        // SmsClient client = new SmsClient(cred, "ap-guangzhou", clientProfile);
        // SendSmsRequest req = new SendSmsRequest();
        // req.setPhoneNumberSet(new String[]{"+86" + phone});
        // req.setSmsSdkAppId(smsSdkAppId);
        // req.setSignName("医药商城");
        // req.setTemplateId(templateId);
        // req.setTemplateParamSet(new String[]{code});
        // SendSmsResponse resp = client.SendSms(req);
    }
}

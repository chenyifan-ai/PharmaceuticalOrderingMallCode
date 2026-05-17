package com.yao.pharmacymall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.pharmacymall.entity.EnterpriseQualification;
import com.yao.pharmacymall.entity.User;
import com.yao.pharmacymall.enums.QualificationStatus;
import com.yao.pharmacymall.service.EnterpriseQualificationService;
import com.yao.pharmacymall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 为采购方测试账号补充企业资质演示数据
 */
@Slf4j
@Component
@Order(28)
public class QualificationSeedInitializer implements CommandLineRunner {

    @Autowired
    private EnterpriseQualificationService qualificationService;
    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) {
        User buyer = getUserByPhone("13800000002");
        if (buyer == null) {
            return;
        }
        EnterpriseQualification existing = qualificationService.getByUserId(buyer.getId());
        if (existing != null) {
            return;
        }
        EnterpriseQualification q = new EnterpriseQualification();
        q.setUserId(buyer.getId());
        q.setCompanyName("某某连锁药店有限公司");
        q.setCreditCode("91110000MA01234567");
        q.setLegalPerson("刘采购");
        q.setLegalPersonIdCard("110101199001011234");
        q.setBusinessLicenseUrl("/images/products/placeholder.jpg");
        q.setDrugOperationPermitUrl("/images/products/placeholder.jpg");
        q.setQualificationStatus(QualificationStatus.APPROVED.getCode());
        q.setQualificationExpireDate(LocalDate.now().plusYears(2));
        q.setReviewTime(LocalDateTime.now().minusDays(3));
        q.setReviewerId(1L);
        qualificationService.save(q);
        log.info("已初始化采购方企业资质演示数据");
    }

    private User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.eq(User::getPhone, phone);
        return userService.getOne(w);
    }
}

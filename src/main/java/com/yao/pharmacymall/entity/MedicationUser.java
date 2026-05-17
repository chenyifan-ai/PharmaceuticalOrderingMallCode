package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用药人信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("medication_user")
public class MedicationUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 用药人姓名
     */
    private String name;

    /**
     * 用药人性别: 0-未知, 1-男, 2-女
     */
    private Integer gender;

    /**
     * 用药人年龄
     */
    private Integer age;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 病史
     */
    private String medicalHistory;

    /**
     * 是否默认用药人: 0-否, 1-是
     */
    private Integer isDefault;
}

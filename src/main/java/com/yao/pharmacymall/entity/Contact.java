package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 联系人实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contact")
public class Contact extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 联系人姓名
     */
    private String name;

    /**
     * 联系人电话
     */
    private String phone;

    /**
     * 职位
     */
    private String position;

    /**
     * 是否为默认联系人：0-否，1-是
     */
    private Integer isDefault;
}
package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("home_banner")
public class HomeBanner extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String subtitle;
    private String imageUrl;
    /** PRODUCT / PACKAGE / SECKILL / NONE */
    private String linkType;
    private String linkValue;
    private Integer sort;
    private Integer status;
}

package com.yao.pharmacymall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.yao.pharmacymall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 通用名
     */
    private String genericName;

    /**
     * 处方类型: OTC-非处方药, PRESCRIPTION-处方药, DUAL_TRACK-双轨制
     */
    private String prescriptionType;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brand;

    /**
     * 规格
     */
    private String specification;

    /**
     * 剂型: 片剂、胶囊、口服液等
     */
    private String dosageForm;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 批准文号
     */
    private String approvalNumber;

    /**
     * 条形码
     */
    private String barcode;

    /**
     * 商品主图URL
     */
    private String mainImage;

    /**
     * 商品图片URLs(JSON数组)
     */
    private String images;

    /**
     * 详情图(JSON数组)
     */
    private String detailImages;

    /**
     * 市场价/划线价
     */
    private BigDecimal marketPrice;

    /**
     * 批发价
     */
    private BigDecimal wholesalePrice;

    /**
     * 阶梯价格(JSON格式)
     */
    private String tierPrices;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 版本号(用于乐观锁)
     */
    @Version
    private Integer version;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 最小起订量
     */
    private Integer minOrderQuantity;

    /**
     * 最大限购量
     */
    private Integer maxOrderQuantity;

    /**
     * 重量(kg)
     */
    private java.math.BigDecimal weight;

    /**
     * 体积(m³)
     */
    private java.math.BigDecimal volume;

    /**
     * 商品状态: 0-下架, 1-上架, 2-待审核
     */
    private Integer status;

    /**
     * 审核状态: 0-待审核, 1-通过, 2-拒绝
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 是否热销: 0-否, 1-是
     */
    private Integer isHot;

    /**
     * 是否新品: 0-否, 1-是
     */
    private Integer isNew;

    /**
     * 是否推荐: 0-否, 1-是
     */
    private Integer isRecommend;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 药品说明书
     */
    private String instruction;

    /**
     * 适应症/功效
     */
    private String indications;

    /**
     * 用法用量
     */
    private String usage;

    /**
     * 禁忌
     */
    private String contraindications;

    /**
     * 不良反应
     */
    private String adverseReactions;

    /**
     * 注意事项
     */
    private String precautions;

    /**
     * 有效期(月)
     */
    private Integer validityPeriod;

    /**
     * 储存条件
     */
    private String storageCondition;

    /**
     * 商品描述
     */
    private String description;
}

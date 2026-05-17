-- ========================================
-- 医药订货系统 - MySQL数据库初始化脚本
-- 版本: v2.0
-- 日期: 2026-05-10
-- 说明: 包含完整的表结构、索引和初始数据
-- ========================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 1. 用户相关表
-- ========================================

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 1-消费者, 2-药店, 3-医院, 4-供应商, 5-管理员, 6-药师',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `id_card` VARCHAR(20) COMMENT '身份证号(AES加密)',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `age` INT COMMENT '年龄',
    `birthday` DATE COMMENT '生日',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `email` VARCHAR(100) COMMENT '邮箱',
    `real_name_status` TINYINT DEFAULT 0 COMMENT '实名认证状态: 0-未认证, 1-已认证, 2-审核中',
    `status` TINYINT DEFAULT 0 COMMENT '账号状态: 0-正常, 1-冻结',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_phone (`phone`),
    INDEX idx_user_type (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户收货地址表
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) NOT NULL COMMENT '区县',
    `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认: 0-否, 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- 用药人信息表
DROP TABLE IF EXISTS `medication_user`;
CREATE TABLE `medication_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用药人ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '用药人姓名',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `age` INT COMMENT '年龄',
    `id_card` VARCHAR(20) COMMENT '身份证号',
    `phone` VARCHAR(20) COMMENT '手机号',
    `allergy_history` TEXT COMMENT '过敏史',
    `medical_history` TEXT COMMENT '病史',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认: 0-否, 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药人信息表';

-- ========================================
-- 2. 商家相关表
-- ========================================

-- 商家信息表
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商家ID',
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '关联用户ID',
    `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `company_name` VARCHAR(200) NOT NULL COMMENT '企业名称',
    `unified_social_credit_code` VARCHAR(50) COMMENT '统一社会信用代码',
    `legal_person` VARCHAR(50) COMMENT '法人姓名',
    `business_license` VARCHAR(500) COMMENT '营业执照图片URL',
    `drug_license` VARCHAR(500) COMMENT '药品经营许可证URL',
    `gsp_certificate` VARCHAR(500) COMMENT 'GSP证书URL',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-已通过, 2-已拒绝',
    `audit_remark` VARCHAR(500) COMMENT '审核备注',
    `audit_time` DATETIME COMMENT '审核时间',
    `auditor_id` BIGINT COMMENT '审核人ID',
    `shop_logo` VARCHAR(500) COMMENT '店铺Logo',
    `shop_banner` VARCHAR(500) COMMENT '店铺Banner',
    `description` TEXT COMMENT '店铺描述',
    `business_scope` VARCHAR(500) COMMENT '经营范围',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `contact_email` VARCHAR(100) COMMENT '联系邮箱',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) COMMENT '城市',
    `district` VARCHAR(50) COMMENT '区县',
    `address` VARCHAR(500) COMMENT '详细地址',
    `rating` DECIMAL(3,2) DEFAULT 5.00 COMMENT '店铺评分',
    `sales_count` INT DEFAULT 0 COMMENT '销量统计',
    `deposit` DECIMAL(10,2) DEFAULT 0 COMMENT '保证金',
    `settlement_account` VARCHAR(100) COMMENT '结算账户',
    `settlement_bank` VARCHAR(100) COMMENT '开户行',
    `commission_rate` DECIMAL(5,2) DEFAULT 5.00 COMMENT '平台佣金比例%',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_audit_status (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家信息表';

-- ========================================
-- 3. 商品相关表
-- ========================================

-- 商品分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
    `level` TINYINT DEFAULT 1 COMMENT '分类层级',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `icon` VARCHAR(255) COMMENT '分类图标',
    `description` VARCHAR(500) COMMENT '分类描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `generic_name` VARCHAR(200) COMMENT '通用名',
    `brand` VARCHAR(100) COMMENT '品牌',
    `specification` VARCHAR(100) COMMENT '规格',
    `manufacturer` VARCHAR(200) COMMENT '生产厂家',
    `approval_number` VARCHAR(50) COMMENT '批准文号',
    `barcode` VARCHAR(50) COMMENT '条形码',
    `product_type` TINYINT NOT NULL COMMENT '商品类型: 1-OTC, 2-处方药, 3-医疗器械, 4-保健品',
    `dosage_form` VARCHAR(50) COMMENT '剂型',
    `description` TEXT COMMENT '商品描述',
    `instruction` JSON COMMENT '药品说明书JSON',
    `indications` TEXT COMMENT '适应症/功效',
    `usage` TEXT COMMENT '用法用量',
    `contraindications` TEXT COMMENT '禁忌',
    `adverse_reactions` TEXT COMMENT '不良反应',
    `precautions` TEXT COMMENT '注意事项',
    `market_price` DECIMAL(10,2) COMMENT '市场价/划线价',
    `wholesale_price` DECIMAL(10,2) NOT NULL COMMENT '批发价',
    `tier_prices` JSON COMMENT '阶梯价格JSON: [{"minQty":1,"maxQty":10,"price":15.00}]',
    `stock` INT DEFAULT 0 COMMENT '总库存',
    `version` INT DEFAULT 0 COMMENT '版本号(用于乐观锁)',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `min_order_qty` INT DEFAULT 1 COMMENT '最小起订量',
    `max_order_qty` INT COMMENT '最大限购量',
    `weight` DECIMAL(10,2) COMMENT '重量kg',
    `volume` DECIMAL(10,2) COMMENT '体积m³',
    `validity_period` INT COMMENT '有效期(月)',
    `storage_condition` VARCHAR(200) COMMENT '储存条件',
    `main_image` VARCHAR(500) COMMENT '主图URL',
    `images` JSON COMMENT '图片列表JSON数组',
    `detail_images` JSON COMMENT '详情图JSON数组',
    `is_hot` TINYINT DEFAULT 0 COMMENT '是否热销: 0-否, 1-是',
    `is_new` TINYINT DEFAULT 0 COMMENT '是否新品: 0-否, 1-是',
    `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐: 0-否, 1-是',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `status` TINYINT DEFAULT 2 COMMENT '上下架状态: 0-下架, 1-上架, 2-待审核',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-通过, 2-拒绝',
    `audit_remark` VARCHAR(500) COMMENT '审核备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`merchant_id`) REFERENCES `merchant`(`id`),
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`),
    INDEX idx_merchant_id (`merchant_id`),
    INDEX idx_category_id (`category_id`),
    INDEX idx_product_type (`product_type`),
    INDEX idx_status (`status`),
    INDEX idx_audit_status (`audit_status`),
    FULLTEXT INDEX ft_product_search (`product_name`, `generic_name`, `brand`, `manufacturer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品批次表（有效期管理）
DROP TABLE IF EXISTS `product_batch`;
CREATE TABLE `product_batch` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '批次ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `batch_number` VARCHAR(50) NOT NULL COMMENT '批次号',
    `production_date` DATE NOT NULL COMMENT '生产日期',
    `expiry_date` DATE NOT NULL COMMENT '有效期至',
    `stock` INT DEFAULT 0 COMMENT '批次库存',
    `locked_stock` INT DEFAULT 0 COMMENT '锁定库存',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`),
    INDEX idx_product_id (`product_id`),
    INDEX idx_expiry_date (`expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品批次表';

-- ========================================
-- 4. 购物车表
-- ========================================

DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车项ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) COMMENT '商品名称(快照)',
    `product_image` VARCHAR(500) COMMENT '商品图片(快照)',
    `price` DECIMAL(10,2) COMMENT '商品价格(快照)',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `checked` TINYINT DEFAULT 1 COMMENT '是否选中: 0-否, 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`),
    INDEX idx_user_id (`user_id`),
    UNIQUE KEY uk_user_product (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ========================================
-- 5. 订单相关表
-- ========================================

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '采购方用户ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `prescription_id` BIGINT COMMENT '关联处方ID（处方药订单）',
    `order_type` TINYINT NOT NULL COMMENT '订单类型: 1-OTC, 2-处方药',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
    `freight` DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `pay_type` TINYINT COMMENT '支付方式: 1-微信, 2-支付宝, 3-银联, 4-对公转账, 5-账期',
    `pay_time` DATETIME COMMENT '支付时间',
    `transaction_no` VARCHAR(100) COMMENT '交易流水号',
    `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待付款, 1-待审核, 2-待发货, 3-已发货, 4-已完成, 5-已取消, 6-退款中, 7-已退款',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    `receiver_address` VARCHAR(500) NOT NULL COMMENT '收货地址',
    `medication_user_id` BIGINT COMMENT '用药人ID',
    `logistics_company` VARCHAR(100) COMMENT '物流公司',
    `logistics_no` VARCHAR(100) COMMENT '运单号',
    `ship_time` DATETIME COMMENT '发货时间',
    `receive_time` DATETIME COMMENT '收货时间',
    `cancel_time` DATETIME COMMENT '取消时间',
    `cancel_reason` VARCHAR(500) COMMENT '取消原因',
    `remark` VARCHAR(500) COMMENT '订单备注',
    `invoice_title` VARCHAR(200) COMMENT '发票抬头',
    `invoice_tax_no` VARCHAR(50) COMMENT '税号',
    `invoice_status` TINYINT DEFAULT 0 COMMENT '发票状态: 0-未开票, 1-已开票',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`merchant_id`) REFERENCES `merchant`(`id`),
    FOREIGN KEY (`prescription_id`) REFERENCES `prescription`(`id`),
    INDEX idx_order_no (`order_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_merchant_id (`merchant_id`),
    INDEX idx_order_status (`order_status`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单商品项表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) COMMENT '商品名称(快照)',
    `product_image` VARCHAR(500) COMMENT '商品图片(快照)',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格(快照)',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    `specification` VARCHAR(100) COMMENT '规格(快照)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`order_id`) REFERENCES `order`(`id`),
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`),
    INDEX idx_order_id (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品项表';

-- 订单状态变更日志表
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `old_status` TINYINT NOT NULL COMMENT '旧状态',
    `new_status` TINYINT NOT NULL COMMENT '新状态',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_type` TINYINT COMMENT '操作人类型: 1-用户, 2-商家, 3-管理员, 4-系统',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (`order_id`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更日志表';

-- ========================================
-- 6. 处方相关表
-- ========================================

DROP TABLE IF EXISTS `prescription`;
CREATE TABLE `prescription` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '处方ID',
    `prescription_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '处方编号',
    `user_id` BIGINT NOT NULL COMMENT '开方用户ID',
    `medication_user_id` BIGINT COMMENT '用药人ID',
    `prescription_type` TINYINT NOT NULL COMMENT '处方类型: 1-用户上传, 2-电子处方, 3-在线问诊',
    `diagnosis` VARCHAR(500) COMMENT '诊断信息',
    `image_urls` JSON COMMENT '处方图片URL列表JSON数组',
    `doctor_name` VARCHAR(50) COMMENT '医生姓名',
    `hospital_name` VARCHAR(200) COMMENT '医院名称',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-通过, 2-拒绝, 3-过期',
    `auditor_id` BIGINT COMMENT '审核药师ID',
    `audit_time` DATETIME COMMENT '审核时间',
    `audit_remark` VARCHAR(500) COMMENT '审核备注',
    `expire_time` DATETIME COMMENT '处方有效期',
    `signature_url` VARCHAR(500) COMMENT '电子签名URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`auditor_id`) REFERENCES `user`(`id`),
    INDEX idx_prescription_no (`prescription_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_audit_status (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方表';

-- ========================================
-- 7. 支付相关表
-- ========================================

DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付ID',
    `payment_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '支付流水号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `payment_method` TINYINT NOT NULL COMMENT '支付方式: 1-微信, 2-支付宝, 3-银联, 4-对公转账, 5-账期',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态: 0-待支付, 1-支付成功, 2-支付失败, 3-已退款',
    `transaction_no` VARCHAR(100) COMMENT '第三方交易号',
    `payment_time` DATETIME COMMENT '支付时间',
    `callback_time` DATETIME COMMENT '回调时间',
    `callback_data` TEXT COMMENT '回调数据(JSON)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`order_id`) REFERENCES `order`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`merchant_id`) REFERENCES `merchant`(`id`),
    INDEX idx_payment_no (`payment_no`),
    INDEX idx_order_id (`order_id`),
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- ========================================
-- 8. 发票表
-- ========================================

DROP TABLE IF EXISTS `invoice`;
CREATE TABLE `invoice` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '发票ID',
    `invoice_no` VARCHAR(50) UNIQUE COMMENT '发票号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `invoice_title` VARCHAR(100) COMMENT '发票抬头',
    `tax_number` VARCHAR(50) COMMENT '纳税人识别号',
    `invoice_type` TINYINT NOT NULL COMMENT '发票类型: 1-个人, 2-企业',
    `invoice_content` VARCHAR(200) COMMENT '发票内容',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '发票金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '发票状态: 0-待开票, 1-已开票, 2-已寄送, 3-已作废',
    `receiver_name` VARCHAR(50) COMMENT '收件人姓名',
    `receiver_phone` VARCHAR(20) COMMENT '收件人手机号',
    `receiver_address` VARCHAR(300) COMMENT '收件地址',
    `logistics_company` VARCHAR(50) COMMENT '快递公司',
    `logistics_no` VARCHAR(100) COMMENT '快递单号',
    `invoice_time` DATETIME COMMENT '开票时间',
    `send_time` DATETIME COMMENT '寄送时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`order_id`) REFERENCES `order`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`merchant_id`) REFERENCES `merchant`(`id`),
    INDEX idx_order_id (`order_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_merchant_id (`merchant_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- ========================================
-- 9. 消息通知表
-- ========================================

DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `message_type` TINYINT NOT NULL COMMENT '消息类型: 1-订单通知, 2-处方审核, 3-物流更新, 4-促销推送, 5-用药提醒, 6-系统通知',
    `title` VARCHAR(100) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `business_id` BIGINT COMMENT '关联业务ID',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `read_time` DATETIME COMMENT '阅读时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_is_read (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- ========================================
-- 10. 插入初始数据
-- ========================================

-- 默认测试账号（密码统一为: admin123，BCrypt加密）
-- 管理员账号：13800000000 / admin123 (user_type=5)
-- 商家账号：13800000001 / admin123 (user_type=4)
-- 普通用户：13800000002 / admin123 (user_type=1)
-- 药师账号：13800000003 / admin123 (user_type=6)
INSERT INTO `user` (`phone`, `password`, `user_type`, `nickname`, `real_name`, `status`, `real_name_status`) VALUES
('13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 5, '管理员', '系统管理员', 0, 1),
('13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 4, '测试商家', '商家用户', 0, 1),
('13800000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, '测试用户', '普通用户', 0, 1),
('13800000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 6, '测试药师', '药师用户', 0, 1);

-- 初始商家数据
INSERT INTO `merchant` (`user_id`, `shop_name`, `company_name`, `unified_social_credit_code`, `legal_person`, `audit_status`, `status`) VALUES
(2, '测试药店', '测试医药有限公司', '91110000MA01234567', '张三', 1, 1);

-- 初始商品分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort`, `status`) VALUES
('OTC药品', 0, 1, 1, 1),
('处方药', 0, 1, 2, 1),
('医疗器械', 0, 1, 3, 1),
('保健品', 0, 1, 4, 1);

INSERT INTO `category` (`name`, `parent_id`, `level`, `sort`, `status`) VALUES
('感冒发烧', 1, 2, 1, 1),
('心脑血管', 2, 2, 1, 1),
('消化系统', 1, 2, 2, 1),
('维生素矿物质', 4, 2, 1, 1),
('家用器械', 3, 2, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库初始化完成！' AS message;
SELECT COUNT(*) AS user_count FROM `user`;
SELECT COUNT(*) AS merchant_count FROM `merchant`;
SELECT COUNT(*) AS category_count FROM `category`;

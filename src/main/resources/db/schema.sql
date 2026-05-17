-- 医药商城数据库初始化脚本
-- 注意：H2数据库会自动使用内存数据库，无需CREATE DATABASE

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(加密)',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 1-消费者, 2-药店, 3-医院, 4-供应商, 5-管理员, 6-药师',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `id_card` VARCHAR(20) COMMENT '身份证号',
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

-- 商品分类表
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
CREATE TABLE `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `product_type` TINYINT NOT NULL COMMENT '商品类型: 1-OTC, 2-处方药, 3-医疗器械, 4-保健品',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `brand` VARCHAR(50) COMMENT '品牌',
    `specification` VARCHAR(100) COMMENT '规格',
    `dosage_form` VARCHAR(50) COMMENT '剂型',
    `manufacturer` VARCHAR(100) COMMENT '生产厂家',
    `approval_number` VARCHAR(50) COMMENT '批准文号',
    `barcode` VARCHAR(50) COMMENT '条形码',
    `main_image` VARCHAR(255) COMMENT '主图URL',
    `images` TEXT COMMENT '图片URLs(逗号分隔)',
    `original_price` DECIMAL(10,2) COMMENT '原价',
    `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `version` INT DEFAULT 0 COMMENT '版本号(用于乐观锁)',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `status` TINYINT DEFAULT 2 COMMENT '状态: 0-下架, 1-上架, 2-待审核',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `is_hot` TINYINT DEFAULT 0 COMMENT '是否热销',
    `is_new` TINYINT DEFAULT 0 COMMENT '是否新品',
    `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `instruction` TEXT COMMENT '药品说明书',
    `indications` TEXT COMMENT '适应症/功效',
    `usage` TEXT COMMENT '用法用量',
    `contraindications` TEXT COMMENT '禁忌',
    `adverse_reactions` TEXT COMMENT '不良反应',
    `precautions` TEXT COMMENT '注意事项',
    `validity_period` INT COMMENT '有效期(月)',
    `storage_condition` VARCHAR(200) COMMENT '储存条件',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_category_id (`category_id`),
    INDEX idx_merchant_id (`merchant_id`),
    INDEX idx_product_type (`product_type`),
    INDEX idx_status (`status`),
    FULLTEXT INDEX ft_name_indications (`name`, `indications`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品批次表(有效期管理)
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
    INDEX idx_product_id (`product_id`),
    INDEX idx_expiry_date (`expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品批次表';

-- 购物车表
CREATE TABLE `cart_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车项ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) COMMENT '商品名称(快照)',
    `product_image` VARCHAR(255) COMMENT '商品图片(快照)',
    `price` DECIMAL(10,2) COMMENT '商品价格(快照)',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `checked` TINYINT DEFAULT 1 COMMENT '是否选中: 0-否, 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (`user_id`),
    UNIQUE KEY uk_user_product (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 订单表
CREATE TABLE `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待付款, 1-待审核, 2-待发货, 3-已发货, 4-已完成, 5-已取消, 6-退款中, 7-已退款',
    `order_type` TINYINT NOT NULL COMMENT '订单类型: 1-OTC, 2-处方药',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    `receiver_address` VARCHAR(300) NOT NULL COMMENT '收货地址',
    `medication_user_id` BIGINT COMMENT '用药人ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
    `discount_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    `freight_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '运费',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `payment_method` TINYINT COMMENT '支付方式: 1-微信, 2-支付宝, 3-银联, 4-医保',
    `payment_time` DATETIME COMMENT '支付时间',
    `transaction_no` VARCHAR(100) COMMENT '交易流水号',
    `delivery_time` DATETIME COMMENT '发货时间',
    `logistics_company` VARCHAR(50) COMMENT '物流公司',
    `logistics_no` VARCHAR(100) COMMENT '物流单号',
    `receive_time` DATETIME COMMENT '收货时间',
    `cancel_time` DATETIME COMMENT '取消时间',
    `cancel_reason` VARCHAR(200) COMMENT '取消原因',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `remark` VARCHAR(500) COMMENT '备注',
    `invoice_status` TINYINT DEFAULT 0 COMMENT '发票状态: 0-未开票, 1-已开票',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_order_no (`order_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_merchant_id (`merchant_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单商品项表
CREATE TABLE `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) COMMENT '商品名称(快照)',
    `product_image` VARCHAR(255) COMMENT '商品图片(快照)',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格(快照)',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    `specification` VARCHAR(100) COMMENT '规格(快照)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_order_id (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品项表';

-- 处方表
CREATE TABLE `prescription` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '处方ID',
    `prescription_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '处方编号',
    `order_id` BIGINT COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `medication_user_id` BIGINT COMMENT '用药人ID',
    `prescription_type` TINYINT NOT NULL COMMENT '处方类型: 1-用户上传, 2-电子处方, 3-在线问诊',
    `image_url` VARCHAR(255) COMMENT '处方图片URL',
    `hospital_id` BIGINT COMMENT '医院/诊所ID',
    `doctor_name` VARCHAR(50) COMMENT '医生姓名',
    `doctor_license_no` VARCHAR(50) COMMENT '医生执业证书号',
    `diagnosis` TEXT COMMENT '诊断信息',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-通过, 2-拒绝, 3-过期',
    `pharmacist_id` BIGINT COMMENT '审核药师ID',
    `audit_time` DATETIME COMMENT '审核时间',
    `audit_remark` VARCHAR(500) COMMENT '审核意见',
    `expiry_time` DATETIME COMMENT '处方有效期',
    `signature_url` VARCHAR(255) COMMENT '电子签名URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_prescription_no (`prescription_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_order_id (`order_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方表';

-- 支付记录表
CREATE TABLE `payment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付ID',
    `payment_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '支付流水号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `payment_method` TINYINT NOT NULL COMMENT '支付方式: 1-微信, 2-支付宝, 3-银联, 4-医保',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态: 0-待支付, 1-支付成功, 2-支付失败, 3-已退款',
    `transaction_no` VARCHAR(100) COMMENT '第三方交易号',
    `payment_time` DATETIME COMMENT '支付时间',
    `callback_time` DATETIME COMMENT '回调时间',
    `callback_data` TEXT COMMENT '回调数据(JSON)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_payment_no (`payment_no`),
    INDEX idx_order_id (`order_id`),
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 消息通知表
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
    INDEX idx_user_id (`user_id`),
    INDEX idx_is_read (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 发票表
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
    INDEX idx_order_id (`order_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_merchant_id (`merchant_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- 商家信息表
CREATE TABLE `merchant` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商家ID',
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '关联用户ID',
    `merchant_name` VARCHAR(100) NOT NULL COMMENT '商家名称',
    `logo` VARCHAR(255) COMMENT '商家Logo',
    `business_license` VARCHAR(50) COMMENT '营业执照号',
    `business_license_image` VARCHAR(255) COMMENT '营业执照图片',
    `legal_person` VARCHAR(50) COMMENT '法人姓名',
    `legal_person_id_card` VARCHAR(20) COMMENT '法人身份证号',
    `legal_person_id_front` VARCHAR(255) COMMENT '法人身份证正面图片',
    `legal_person_id_back` VARCHAR(255) COMMENT '法人身份证反面图片',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `contact_email` VARCHAR(100) COMMENT '联系邮箱',
    `shop_address` VARCHAR(300) COMMENT '店铺地址',
    `description` TEXT COMMENT '店铺描述',
    `business_scope` VARCHAR(500) COMMENT '经营范围',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-已通过, 2-已拒绝',
    `audit_remark` VARCHAR(500) COMMENT '审核备注',
    `audit_time` DATETIME COMMENT '审核时间',
    `auditor_id` BIGINT COMMENT '审核人ID',
    `rating` DOUBLE DEFAULT 5.0 COMMENT '店铺评分',
    `sales_count` INT DEFAULT 0 COMMENT '销量统计',
    `deposit` DECIMAL(10,2) DEFAULT 0 COMMENT '保证金',
    `settlement_account_type` TINYINT DEFAULT 1 COMMENT '结算账户类型: 1-银行卡, 2-支付宝',
    `settlement_account_no` VARCHAR(50) COMMENT '结算账户号',
    `settlement_account_name` VARCHAR(100) COMMENT '结算账户名',
    `bank_name` VARCHAR(100) COMMENT '开户行',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (`user_id`),
    INDEX idx_audit_status (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家信息表';

-- 插入初始数据

-- 默认测试账号（密码统一为: admin123，BCrypt加密）
-- 管理员账号：13800000000 / admin123 (user_type=5)
-- 商家账号：13800000001 / admin123 (user_type=4)
-- 普通用户：13800000002 / admin123 (user_type=1)
INSERT INTO `user` (`phone`, `password`, `user_type`, `nickname`, `real_name`, `status`, `real_name_status`) VALUES
('13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 5, '管理员', '系统管理员', 0, 1),
('13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 4, '测试商家', '商家用户', 0, 1),
('13800000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, '测试用户', '普通用户', 0, 1);

-- 初始商品分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort`, `status`) VALUES
('OTC药品', 0, 1, 1, 1),
('处方药', 0, 1, 2, 1),
('医疗器械', 0, 1, 3, 1),
('保健品', 0, 1, 4, 1);

INSERT INTO `category` (`name`, `parent_id`, `level`, `sort`, `status`) VALUES
('感冒发烧', 1, 2, 1, 1),
('心脑血管', 1, 2, 2, 1),
('消化系统', 1, 2, 3, 1),
('维生素矿物质', 4, 2, 1, 1);

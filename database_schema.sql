-- 医药订货系统数据库表结构设计

-- 用户表 (users)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    username VARCHAR(50) COMMENT '用户名',
    user_type ENUM('C', 'B_ADMIN', 'B_SUPPLIER', 'B_PHARMACIST') NOT NULL DEFAULT 'C' COMMENT '用户类型：C-采购方，B_ADMIN-平台管理员，B_SUPPLIER-供应商，B_PHARMACIST-药师',
    status ENUM('ACTIVE', 'INACTIVE', 'FROZEN') DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，INACTIVE-未激活，FROZEN-冻结',
    avatar VARCHAR(255) COMMENT '头像URL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 企业资质信息表 (enterprise_qualifications)
CREATE TABLE enterprise_qualifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    enterprise_name VARCHAR(200) NOT NULL COMMENT '企业名称',
    unified_social_credit_code VARCHAR(50) NOT NULL COMMENT '统一社会信用代码',
    legal_representative VARCHAR(50) COMMENT '法定代表人',
    business_license_image VARCHAR(255) COMMENT '营业执照照片',
    drug_operation_permit_image VARCHAR(255) COMMENT '药品经营许可证照片',
    medical_device_permit_image VARCHAR(255) COMMENT '医疗器械经营许可证照片',
    gsp_certificate_image VARCHAR(255) COMMENT 'GSP认证证书照片',
    qualification_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '资质审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-未通过',
    qualification_reject_reason TEXT COMMENT '资质驳回原因',
    qualification_expire_date DATE COMMENT '资质到期日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (qualification_status)
);

-- 联系人表 (contacts)
CREATE TABLE contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    phone VARCHAR(20) COMMENT '联系人电话',
    position VARCHAR(50) COMMENT '职位',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否为默认联系人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id)
);

-- 收货地址表 (shipping_addresses)
CREATE TABLE shipping_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收件人姓名',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区县',
    detail_address VARCHAR(200) NOT NULL COMMENT '详细地址',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否为默认地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id)
);

-- 商品分类表 (categories)
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
    level INT DEFAULT 1 COMMENT '分类层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level)
);

-- 品牌表 (brands)
CREATE TABLE brands (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '品牌名称',
    logo VARCHAR(255) COMMENT '品牌logo',
    description TEXT COMMENT '品牌描述',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 商品表 (products)
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    generic_name VARCHAR(200) COMMENT '通用名',
    specification VARCHAR(100) COMMENT '规格',
    dosage_form VARCHAR(50) COMMENT '剂型',
    manufacturer VARCHAR(200) COMMENT '生产厂家',
    approval_number VARCHAR(100) COMMENT '批准文号',
    barcode VARCHAR(50) COMMENT '条形码',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    brand_id BIGINT COMMENT '品牌ID',
    prescription_type ENUM('OTC', 'PRESCRIPTION', 'DUAL_TRACK') DEFAULT 'OTC' COMMENT '处方类型：OTC-非处方药，PRESCRIPTION-处方药，DUAL_TRACK-双轨制',
    market_price DECIMAL(10,2) COMMENT '市场价',
    wholesale_price DECIMAL(10,2) COMMENT '批发价',
    description TEXT COMMENT '商品描述',
    product_images JSON COMMENT '商品图片列表(JSON)',
    instruction TEXT COMMENT '药品说明书',
    storage_condition VARCHAR(200) COMMENT '贮藏条件',
    shelf_life VARCHAR(100) COMMENT '有效期',
    weight DECIMAL(8,2) COMMENT '重量(kg)',
    volume DECIMAL(8,2) COMMENT '体积(m³)',
    min_order_quantity INT DEFAULT 1 COMMENT '最小起订量',
    max_order_quantity INT COMMENT '最大订购量',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING_REVIEW') DEFAULT 'PENDING_REVIEW' COMMENT '状态：ACTIVE-上架，INACTIVE-下架，PENDING_REVIEW-待审核',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (brand_id) REFERENCES brands(id),
    FOREIGN KEY (supplier_id) REFERENCES users(id),
    INDEX idx_category_id (category_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status),
    INDEX idx_prescription_type (prescription_type)
);

-- 商品库存表 (product_stocks)
CREATE TABLE product_stocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    warning_quantity INT DEFAULT 0 COMMENT '库存预警值',
    batch_number VARCHAR(100) COMMENT '批号',
    production_date DATE COMMENT '生产日期',
    expire_date DATE COMMENT '有效期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_product_id (product_id),
    INDEX idx_expire_date (expire_date)
);

-- 阶梯价格表 (tier_prices)
CREATE TABLE tier_prices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    min_quantity INT NOT NULL COMMENT '最小数量',
    max_quantity INT COMMENT '最大数量(null表示无上限)',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_product_id (product_id)
);

-- 购物车表 (shopping_cart_items)
CREATE TABLE shopping_cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
);

-- 订单表 (orders)
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) UNIQUE NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    freight_amount DECIMAL(10,2) DEFAULT 0 COMMENT '运费',
    final_amount DECIMAL(12,2) NOT NULL COMMENT '最终金额',
    status ENUM('PENDING_PAYMENT', 'PENDING_REVIEW', 'PAID', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态',
    shipping_address_id BIGINT COMMENT '收货地址ID',
    contact_id BIGINT COMMENT '联系人ID',
    invoice_info JSON COMMENT '发票信息(JSON)',
    remark TEXT COMMENT '订单备注',
    payment_method VARCHAR(20) COMMENT '支付方式',
    payment_time TIMESTAMP NULL COMMENT '支付时间',
    shipped_time TIMESTAMP NULL COMMENT '发货时间',
    delivered_time TIMESTAMP NULL COMMENT '收货时间',
    cancelled_time TIMESTAMP NULL COMMENT '取消时间',
    cancelled_reason VARCHAR(200) COMMENT '取消原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (shipping_address_id) REFERENCES shipping_addresses(id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 订单商品表 (order_items)
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_specification VARCHAR(100) COMMENT '商品规格',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    subtotal DECIMAL(12,2) NOT NULL COMMENT '小计',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order_id (order_id)
);

-- 处方表 (prescriptions)
CREATE TABLE prescriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT COMMENT '关联订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    prescription_image VARCHAR(255) COMMENT '处方图片',
    electronic_prescription_id VARCHAR(100) COMMENT '电子处方ID',
    diagnosis TEXT COMMENT '诊断信息',
    doctor_name VARCHAR(50) COMMENT '医生姓名',
    hospital_name VARCHAR(200) COMMENT '医院名称',
    status ENUM('PENDING_REVIEW', 'APPROVED', 'REJECTED') DEFAULT 'PENDING_REVIEW' COMMENT '审核状态',
    reviewer_id BIGINT COMMENT '审核药师ID',
    review_time TIMESTAMP NULL COMMENT '审核时间',
    reject_reason TEXT COMMENT '驳回原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status)
);

-- 物流信息表 (logistics_info)
CREATE TABLE logistics_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    express_company VARCHAR(100) COMMENT '快递公司',
    tracking_number VARCHAR(100) COMMENT '运单号',
    status VARCHAR(50) COMMENT '物流状态',
    current_location VARCHAR(200) COMMENT '当前位置',
    update_time TIMESTAMP COMMENT '更新时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order_id (order_id),
    INDEX idx_tracking_number (tracking_number)
);

-- 支付记录表 (payment_records)
CREATE TABLE payment_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    payment_no VARCHAR(50) UNIQUE NOT NULL COMMENT '支付流水号',
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式',
    amount DECIMAL(12,2) NOT NULL COMMENT '支付金额',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING' COMMENT '支付状态',
    transaction_id VARCHAR(100) COMMENT '第三方交易号',
    payment_time TIMESTAMP NULL COMMENT '支付时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order_id (order_id),
    INDEX idx_payment_no (payment_no)
);

-- 优惠券表 (coupons)
CREATE TABLE coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type ENUM('DISCOUNT', 'CASH') NOT NULL COMMENT '类型：DISCOUNT-折扣券，CASH-现金券',
    discount_value DECIMAL(10,2) COMMENT '折扣值',
    min_order_amount DECIMAL(10,2) COMMENT '最低订单金额',
    total_count INT NOT NULL DEFAULT 0 COMMENT '总发行量',
    received_count INT DEFAULT 0 COMMENT '已领取数量',
    valid_start_date DATE NOT NULL COMMENT '有效开始日期',
    valid_end_date DATE NOT NULL COMMENT '有效结束日期',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户优惠券表 (user_coupons)
CREATE TABLE user_coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    status ENUM('UNUSED', 'USED', 'EXPIRED') DEFAULT 'UNUSED' COMMENT '状态：UNUSED-未使用，USED-已使用，EXPIRED-已过期',
    used_time TIMESTAMP NULL COMMENT '使用时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id)
);

-- 系统消息表 (messages)
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    type ENUM('ORDER', 'PRESCRIPTION', 'LOGISTICS', 'SYSTEM', 'MARKETING') COMMENT '消息类型',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    related_id BIGINT COMMENT '关联ID（如订单ID、处方ID等）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
);
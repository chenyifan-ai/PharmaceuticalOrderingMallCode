-- H2 Database Schema for Development
-- 注意：使用H2 MySQL兼容模式，支持反引号``和INSERT IGNORE语法

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    user_type TINYINT NOT NULL DEFAULT 1,
    nickname VARCHAR(50),
    real_name VARCHAR(50),
    id_card VARCHAR(20),
    gender TINYINT DEFAULT 0,
    age INT,
    birthday DATE,
    avatar VARCHAR(255),
    email VARCHAR(100),
    real_name_status TINYINT DEFAULT 0,
    status TINYINT DEFAULT 0,
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_phone ON `user`(phone);
CREATE INDEX IF NOT EXISTS idx_user_type ON `user`(user_type);

-- 用户收货地址表
CREATE TABLE IF NOT EXISTS user_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    detail_address VARCHAR(200) NOT NULL,
    is_default TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_user_id ON user_address(user_id);

-- 用药人信息表
CREATE TABLE IF NOT EXISTS medication_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender TINYINT DEFAULT 0,
    age INT,
    id_card VARCHAR(20),
    phone VARCHAR(20),
    allergy_history TEXT,
    medical_history TEXT,
    is_default TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_medication_user_id ON medication_user(user_id);

-- 商品分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level TINYINT DEFAULT 1,
    sort INT DEFAULT 0,
    icon VARCHAR(255),
    description VARCHAR(500),
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_category_parent_id ON category(parent_id);

-- 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    generic_name VARCHAR(100),
    prescription_type VARCHAR(20) NOT NULL DEFAULT 'OTC',
    category_id BIGINT NOT NULL,
    brand_id BIGINT,
    brand VARCHAR(50),
    specification VARCHAR(100),
    dosage_form VARCHAR(50),
    manufacturer VARCHAR(100),
    approval_number VARCHAR(50),
    barcode VARCHAR(50),
    main_image VARCHAR(255),
    images TEXT,
    detail_images TEXT,
    market_price DECIMAL(10,2),
    wholesale_price DECIMAL(10,2) NOT NULL,
    tier_prices TEXT,
    stock INT DEFAULT 0,
    version INT DEFAULT 0,
    sales INT DEFAULT 0,
    min_order_quantity INT,
    max_order_quantity INT,
    weight DECIMAL(10,2),
    volume DECIMAL(10,2),
    status TINYINT DEFAULT 2,
    audit_status TINYINT DEFAULT 0,
    audit_remark VARCHAR(500),
    supplier_id BIGINT NOT NULL,
    is_hot TINYINT DEFAULT 0,
    is_new TINYINT DEFAULT 0,
    is_recommend TINYINT DEFAULT 0,
    sort INT DEFAULT 0,
    instruction TEXT,
    indications TEXT,
    usage TEXT,
    contraindications TEXT,
    adverse_reactions TEXT,
    precautions TEXT,
    description TEXT,
    validity_period INT,
    storage_condition VARCHAR(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_product_category_id ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_supplier_id ON product(supplier_id);
CREATE INDEX IF NOT EXISTS idx_product_prescription_type ON product(prescription_type);
CREATE INDEX IF NOT EXISTS idx_product_status ON product(status);

-- 商品批次表
CREATE TABLE IF NOT EXISTS product_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    production_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    stock INT DEFAULT 0,
    locked_stock INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_batch_product_id ON product_batch(product_id);
CREATE INDEX IF NOT EXISTS idx_expiry_date ON product_batch(expiry_date);

-- 商品库存扩展表（预警阈值等）
CREATE TABLE IF NOT EXISTS product_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    quantity INT DEFAULT 0,
    warning_quantity INT DEFAULT 10,
    batch_number VARCHAR(50),
    production_date DATE,
    expire_date DATE,
    locked_stock INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_product_stock_product_id ON product_stock(product_id);

-- 库存变动记录
CREATE TABLE IF NOT EXISTS stock_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    change_type TINYINT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_change INT NOT NULL,
    quantity_after INT NOT NULL,
    reason VARCHAR(500),
    operator_id BIGINT,
    operator_name VARCHAR(50),
    supplier_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_stock_log_product_id ON stock_log(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_log_create_time ON stock_log(create_time);

-- 购物车表
CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    seckill_id BIGINT DEFAULT 0,
    product_name VARCHAR(100),
    product_image VARCHAR(255),
    price DECIMAL(10,2),
    quantity INT NOT NULL DEFAULT 1,
    checked TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT uk_user_product_seckill UNIQUE (user_id, product_id, seckill_id)
);

CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart_item(user_id);

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    order_type TINYINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(300) NOT NULL,
    medication_user_id BIGINT,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    user_coupon_id BIGINT,
    freight DECIMAL(10,2) DEFAULT 0,
    pay_amount DECIMAL(10,2) NOT NULL,
    pay_type TINYINT,
    pay_time TIMESTAMP,
    transaction_no VARCHAR(100),
    ship_time TIMESTAMP,
    logistics_company VARCHAR(50),
    logistics_no VARCHAR(100),
    receive_time TIMESTAMP,
    cancel_time TIMESTAMP,
    cancel_reason VARCHAR(200),
    merchant_id BIGINT NOT NULL,
    package_id BIGINT,
    remark VARCHAR(500),
    invoice_title VARCHAR(100),
    invoice_tax_no VARCHAR(50),
    invoice_status TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_order_no ON `order`(order_no);
CREATE INDEX IF NOT EXISTS idx_order_user_id ON `order`(user_id);
CREATE INDEX IF NOT EXISTS idx_order_merchant_id ON `order`(merchant_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON `order`(status);

-- 订单商品项表
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    product_image VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    specification VARCHAR(100),
    seckill_id BIGINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_item(order_id);

-- 支付记录表
CREATE TABLE IF NOT EXISTS payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_method TINYINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    transaction_no VARCHAR(100),
    payment_time TIMESTAMP,
    callback_time TIMESTAMP,
    callback_data TEXT,
    voucher_url VARCHAR(500),
    voucher_status TINYINT DEFAULT 0,
    voucher_reject_reason VARCHAR(500),
    transfer_remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_payment_no ON payment(payment_no);
CREATE INDEX IF NOT EXISTS idx_payment_order_id ON payment(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_user_id ON payment(user_id);

-- 消息通知表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message_type TINYINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    business_id BIGINT,
    is_read TINYINT DEFAULT 0,
    read_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_message_user_id ON message(user_id);
CREATE INDEX IF NOT EXISTS idx_is_read ON message(is_read);

-- 发票表
CREATE TABLE IF NOT EXISTS invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_no VARCHAR(50) UNIQUE,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    invoice_title VARCHAR(100),
    tax_number VARCHAR(50),
    invoice_type TINYINT NOT NULL,
    invoice_content VARCHAR(200),
    amount DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    receiver_name VARCHAR(50),
    receiver_phone VARCHAR(20),
    receiver_address VARCHAR(300),
    logistics_company VARCHAR(50),
    logistics_no VARCHAR(100),
    invoice_time TIMESTAMP,
    send_time TIMESTAMP,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_invoice_order_id ON invoice(order_id);
CREATE INDEX IF NOT EXISTS idx_invoice_user_id ON invoice(user_id);
CREATE INDEX IF NOT EXISTS idx_invoice_merchant_id ON invoice(merchant_id);
CREATE INDEX IF NOT EXISTS idx_invoice_status ON invoice(status);

-- 订单状态变更日志表
CREATE TABLE IF NOT EXISTS order_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    old_status TINYINT NOT NULL,
    new_status TINYINT NOT NULL,
    operator_id BIGINT,
    operator_type TINYINT,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_status_log_order_id ON order_status_log(order_id);

-- 处方表
CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    medication_user_id BIGINT,
    order_id BIGINT,
    prescription_type TINYINT NOT NULL,
    diagnosis VARCHAR(500),
    image_urls CLOB,
    doctor_name VARCHAR(50),
    hospital_name VARCHAR(200),
    audit_status TINYINT DEFAULT 0,
    auditor_id BIGINT,
    audit_time TIMESTAMP,
    audit_remark VARCHAR(500),
    expire_time TIMESTAMP,
    signature_url VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_prescription_user_id ON prescription(user_id);
CREATE INDEX IF NOT EXISTS idx_prescription_audit_status ON prescription(audit_status);

-- 联系人表（B端采购联系人）
CREATE TABLE IF NOT EXISTS contact (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    position VARCHAR(50),
    is_default TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_contact_user_id ON contact(user_id);

-- 商家信息表
CREATE TABLE IF NOT EXISTS merchant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    merchant_name VARCHAR(100) NOT NULL,
    logo VARCHAR(255),
    business_license VARCHAR(50),
    business_license_image VARCHAR(255),
    legal_person VARCHAR(50),
    legal_person_id_card VARCHAR(20),
    legal_person_id_front VARCHAR(255),
    legal_person_id_back VARCHAR(255),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    shop_address VARCHAR(300),
    description TEXT,
    business_scope VARCHAR(500),
    audit_status TINYINT DEFAULT 0,
    audit_remark VARCHAR(500),
    audit_time TIMESTAMP,
    auditor_id BIGINT,
    rating DOUBLE DEFAULT 5.0,
    sales_count INT DEFAULT 0,
    deposit DECIMAL(10,2) DEFAULT 0,
    settlement_account_type TINYINT DEFAULT 1,
    settlement_account_no VARCHAR(50),
    settlement_account_name VARCHAR(100),
    bank_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_merchant_user_id ON merchant(user_id);
CREATE INDEX IF NOT EXISTS idx_merchant_audit_status ON merchant(audit_status);

-- 企业资质认证表
CREATE TABLE IF NOT EXISTS enterprise_qualification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(100),
    credit_code VARCHAR(50),
    legal_person VARCHAR(50),
    legal_person_id_card VARCHAR(20),
    business_license_url VARCHAR(255),
    drug_operation_permit_url VARCHAR(255),
    medical_device_permit_url VARCHAR(255),
    gsp_certificate_url VARCHAR(255),
    qualification_status TINYINT DEFAULT 0,
    qualification_reject_reason VARCHAR(500),
    qualification_expire_date DATE,
    reviewer_id BIGINT,
    review_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_qualification_user_id ON enterprise_qualification(user_id);
CREATE INDEX IF NOT EXISTS idx_qualification_status ON enterprise_qualification(qualification_status);

-- 首页轮播
CREATE TABLE IF NOT EXISTS home_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    subtitle VARCHAR(200),
    image_url VARCHAR(500) NOT NULL,
    link_type VARCHAR(20) DEFAULT 'NONE',
    link_value VARCHAR(100),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 秒杀活动
CREATE TABLE IF NOT EXISTS seckill_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL,
    seckill_price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    sold_count INT DEFAULT 0,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_seckill_product ON seckill_item(product_id);

-- 组合套餐
CREATE TABLE IF NOT EXISTS product_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_name VARCHAR(100) NOT NULL,
    subtitle VARCHAR(200),
    banner_image VARCHAR(500),
    original_price DECIMAL(10,2),
    package_price DECIMAL(10,2) NOT NULL,
    items TEXT NOT NULL,
    stock INT DEFAULT 0,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 优惠券
CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(10,2),
    total_count INT,
    received_count INT DEFAULT 0,
    valid_start_date DATE,
    valid_end_date DATE,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    used_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 物流轨迹
CREATE TABLE IF NOT EXISTS logistics_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    express_company VARCHAR(100),
    tracking_number VARCHAR(100),
    status VARCHAR(50),
    current_location VARCHAR(200),
    update_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_logistics_order ON logistics_info(order_id);

-- 供应商结算单
CREATE TABLE IF NOT EXISTS merchant_settlement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    settlement_no VARCHAR(50) NOT NULL UNIQUE,
    period_start DATE,
    period_end DATE,
    order_count INT DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    platform_fee DECIMAL(12,2) DEFAULT 0,
    settle_amount DECIMAL(12,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    settle_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_settlement_merchant ON merchant_settlement(merchant_id);

-- 操作日志
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    operator_type TINYINT,
    module VARCHAR(50),
    action VARCHAR(100),
    target_id BIGINT,
    detail VARCHAR(1000),
    ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_operation_log_time ON operation_log(create_time);

-- ==================== 插入初始数据 ====================

-- 注意：测试账号由 DataInitializer.java 在应用启动时创建
-- 这样可以确保使用正确的 BCrypt 加密密码

-- 初始商品分类
INSERT IGNORE INTO category (name, parent_id, level, sort, status) VALUES
('OTC药品', 0, 1, 1, 1),
('处方药', 0, 1, 2, 1),
('医疗器械', 0, 1, 3, 1),
('保健品', 0, 1, 4, 1);

INSERT IGNORE INTO category (name, parent_id, level, sort, status) VALUES
('感冒发烧', 1, 2, 1, 1),
('心脑血管', 1, 2, 2, 1),
('消化系统', 1, 2, 3, 1),
('维生素矿物质', 4, 2, 1, 1);

-- 初始商品数据（商家ID使用管理员的ID=1）
INSERT IGNORE INTO product (product_name, prescription_type, category_id, brand, specification, dosage_form, manufacturer, approval_number, main_image, images, detail_images, market_price, wholesale_price, stock, sales, status, supplier_id, is_hot, is_new, sort, indications, usage, precautions) VALUES
('阿莫西林胶囊', 'PRESCRIPTION', 2, '联邦制药', '0.5g*24粒', '胶囊剂', '珠海联邦制药股份有限公司', '国药准字H44021518', '/images/products/amoxicillin.svg', '["/images/products/amoxicillin.svg"]', '["/images/products/amoxicillin.svg"]', 25.00, 18.50, 500, 1280, 1, 1, 1, 0, 1, '适用于敏感菌引起的呼吸道感染、泌尿生殖道感染等', '口服，成人一次0.5g，每6-8小时1次', '对青霉素类药物过敏者禁用'),
('布洛芬缓释胶囊', 'OTC', 5, '中美史克', '0.3g*20粒', '缓释胶囊', '中美天津史克制药有限公司', '国药准字H20013062', '/images/products/ibuprofen.svg', '["/images/products/ibuprofen.svg"]', '["/images/products/ibuprofen.svg"]', 29.80, 22.50, 800, 2560, 1, 1, 1, 0, 2, '用于缓解轻至中度疼痛，如头痛、关节痛等', '口服，成人一次1粒，一日2次', '孕妇及哺乳期妇女慎用'),
('连花清瘟胶囊', 'OTC', 5, '以岭药业', '0.35g*36粒', '胶囊剂', '石家庄以岭药业股份有限公司', '国药准字Z20040063', '/images/products/lianhua.svg', '["/images/products/lianhua.svg"]', '["/images/products/lianhua.svg"]', 24.80, 18.00, 1000, 3800, 1, 1, 1, 1, 3, '清瘟解毒，宣肺泄热，用于治疗流行性感冒', '口服，一次4粒，一日3次', '风寒感冒者不适用'),
('电子血压计', 'OTC', 3, '欧姆龙', 'HEM-7121', '医疗器械', '欧姆龙健康医疗株式会社', '辽械注准20152200123', '/images/products/bp-monitor.svg', '["/images/products/bp-monitor.svg"]', '["/images/products/bp-monitor.svg"]', 299.00, 239.00, 200, 560, 1, 1, 0, 0, 4, '用于测量成人血压及脉搏数', '按照说明书正确佩戴袖带，保持安静状态测量', '心脏起搏器使用者请咨询医生'),
('维生素C咀嚼片', 'OTC', 8, '汤臣倍健', '100片/瓶', '片剂', '汤臣倍健股份有限公司', '国食健字G20110601', '/images/products/vitamin-c.svg', '["/images/products/vitamin-c.svg"]', '["/images/products/vitamin-c.svg"]', 78.00, 58.00, 600, 1800, 1, 1, 0, 1, 5, '补充维生素C，增强免疫力', '每日1次，每次1片，咀嚼食用', '不宜超过推荐量'),
('藿香正气水', 'OTC', 5, '太极集团', '10ml*10支', '合剂', '太极集团重庆涪陵制药厂有限公司', '国药准字Z50020409', '/images/products/huoxiang.svg', '["/images/products/huoxiang.svg"]', '["/images/products/huoxiang.svg"]', 15.00, 9.90, 1500, 4200, 1, 1, 1, 1, 6, '解表化湿，理气和中，用于外感风寒，内伤湿滞', '口服，一次5-10ml，一日2次', '驾驶员慎用'),
('创可贴', 'OTC', 3, '云南白药', '100片/盒', '贴剂', '云南白药集团股份有限公司', '国药准字Z20073016', '/images/products/bandage.svg', '["/images/products/bandage.svg"]', '["/images/products/bandage.svg"]', 25.00, 19.80, 2000, 5000, 1, 1, 0, 0, 7, '用于小创伤、擦伤等浅表性创面的止血和保护', '清洁创面后，撕去包装纸，将吸水垫贴于创面', '若出现过敏反应请立即停止使用'),
('钙维生素D片', 'OTC', 8, '钙尔奇', '60片/瓶', '片剂', '惠氏制药有限公司', '国食健字G20090084', '/images/products/calcium.svg', '["/images/products/calcium.svg"]', '["/images/products/calcium.svg"]', 138.00, 108.00, 400, 920, 1, 1, 0, 1, 8, '用于妊娠和哺乳期妇女、更年期妇女等钙的补充', '每日1次，每次1片，饭后服用', '高钙血症者禁用');

INSERT IGNORE INTO coupon (name, type, discount_value, min_order_amount, total_count, received_count, valid_start_date, valid_end_date, status) VALUES
('新客满减券', 'CASH', 20.00, 100.00, 1000, 0, DATE '2025-01-01', DATE '2027-12-31', 1),
('采购9折券', 'DISCOUNT', 0.90, 200.00, 500, 0, DATE '2025-01-01', DATE '2027-12-31', 1);

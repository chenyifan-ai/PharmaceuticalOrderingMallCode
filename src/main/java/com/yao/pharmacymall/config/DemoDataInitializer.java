package com.yao.pharmacymall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.pharmacymall.entity.*;
import com.yao.pharmacymall.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 演示数据初始化：订单、发票、供应商（在测试账号创建之后执行）
 */
@Slf4j
@Component
@org.springframework.core.annotation.Order(2)
public class DemoDataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private ProductService productService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (orderService.count() > 0) {
            log.debug("演示订单数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化演示数据（订单/发票/供应商）...");

        User consumer = getUserByPhone("13800000002");
        if (consumer == null) {
            log.warn("未找到采购方测试账号，跳过演示数据");
            return;
        }

        User supplierUser = getUserByPhone("13800000001");
        Merchant m1 = null;
        if (supplierUser != null) {
            m1 = ensureMerchant(supplierUser.getId(), "联邦医药批发有限公司", "张建国",
                    "91310000MA1FL2XXXX", "021-58881234", "supplier@federal-pharm.com",
                    "上海市浦东新区张江路88号", "OTC及处方药批发", 1, 4.8, 12580);
        }

        User supplier2 = ensureSupplierUser("13800000003", "齐鲁医药批发", "李经理");
        User supplier3 = ensureSupplierUser("13800000004", "仁济堂药业", "王总");

        Merchant m2 = ensureMerchant(supplier2.getId(), "齐鲁医药批发有限公司", "李明",
                "91370000MA3C5XXXX", "0531-88886666", "contact@qilu-pharm.com",
                "山东省济南市高新区医药园12号", "药品批发、医疗器械", 1, 4.6, 8920);
        ensureMerchant(supplier3.getId(), "仁济堂药业有限公司", "王仁济",
                "91440000MA5D8XXXX", "020-36667777", "info@renjitang.com",
                "广州市白云区医药大道66号", "中成药、保健品批发", 0, 5.0, 0);

        Merchant primary = m1 != null ? m1 : m2;
        Long merchantId = primary.getId();
        List<Product> products = productService.list();
        if (m1 != null) {
            productService.lambdaUpdate()
                    .set(Product::getSupplierId, m1.getId())
                    .le(Product::getSupplierId, 1L)
                    .update();
        }
        if (products.isEmpty()) {
            log.warn("无商品数据，跳过订单演示数据");
            return;
        }

        seedOrdersAndInvoices(consumer.getId(), merchantId, products, m2 != null ? m2.getId() : merchantId);
        log.info("演示数据初始化完成");
    }

    private User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.eq(User::getPhone, phone);
        return userService.getOne(w);
    }

    private User ensureSupplierUser(String phone, String nickname, String realName) {
        User u = getUserByPhone(phone);
        if (u != null) {
            return u;
        }
        u = new User();
        u.setPhone(phone);
        u.setPassword(passwordEncoder.encode("admin123"));
        u.setUserType(4);
        u.setNickname(nickname);
        u.setRealName(realName);
        u.setStatus(0);
        u.setRealNameStatus(1);
        userService.save(u);
        return u;
    }

    private Merchant ensureMerchant(Long userId, String name, String legalPerson, String license,
                                    String phone, String email, String address, String scope,
                                    int auditStatus, double rating, int salesCount) {
        Merchant existing = merchantService.getMerchantByUserId(userId);
        if (existing != null) {
            return existing;
        }
        Merchant m = new Merchant();
        m.setUserId(userId);
        m.setMerchantName(name);
        m.setLegalPerson(legalPerson);
        m.setBusinessLicense(license);
        m.setContactPhone(phone);
        m.setContactEmail(email);
        m.setShopAddress(address);
        m.setBusinessScope(scope);
        m.setDescription(name + " - 平台认证供应商");
        m.setAuditStatus(auditStatus);
        m.setRating(rating);
        m.setSalesCount(salesCount);
        m.setDeposit(new BigDecimal("50000.00"));
        m.setSettlementAccountType(1);
        m.setSettlementAccountName(name);
        m.setBankName("中国工商银行");
        m.setSettlementAccountNo("6222021001234567890");
        if (auditStatus == 1) {
            m.setAuditTime(LocalDateTime.now());
        }
        merchantService.save(m);
        return m;
    }

    private void seedOrdersAndInvoices(Long userId, Long merchantId, List<Product> products, Long merchantId2) {
        Product p1 = products.get(0);
        Product p2 = products.size() > 1 ? products.get(1) : p1;
        Product p3 = products.size() > 2 ? products.get(2) : p1;

        createOrderWithItems("DD20260517001", userId, merchantId, 4, 1,
                "刘采购", "13800000002", "北京市朝阳区建国路100号医药大厦",
                p1, 2, p2, 1, true, 0);
        createOrderWithItems("DD20260517002", userId, merchantId, 3, 1,
                "刘采购", "13800000002", "北京市朝阳区建国路100号医药大厦",
                p2, 5, null, 0, true, 1);
        createOrderWithItems("DD20260517003", userId, merchantId, 2, 1,
                "陈经理", "13912345678", "上海市静安区南京西路200号",
                p3, 10, p1, 3, false, 2);
        createOrderWithItems("DD20260517004", userId, merchantId, 1, 2,
                "陈经理", "13912345678", "上海市静安区南京西路200号",
                p1, 1, null, 0, false, 3);
        createOrderWithItems("DD20260517005", userId, merchantId, 0, 1,
                "赵主任", "13700001111", "广州市天河区体育西路50号",
                p2, 2, null, 0, false, 4);
        createOrderWithItems("DD20260517006", userId, merchantId2, 4, 1,
                "孙药师", "13688889999", "深圳市南山区科技园南路18号",
                p3, 4, p2, 2, true, 1);
        createOrderWithItems("DD20260517007", userId, merchantId, 5, 1,
                "周店长", "13566667777", "杭州市西湖区文三路168号",
                p1, 1, null, 0, false, 0);
        createOrderWithItems("DD20260517008", userId, merchantId, 2, 1,
                "周店长", "13566667777", "杭州市西湖区文三路168号",
                p2, 8, p3, 5, false, 0);
    }

    private void createOrderWithItems(String orderNo, Long userId, Long merchantId, int status, int orderType,
                                      String receiverName, String receiverPhone, String address,
                                      Product pA, int qtyA, Product pB, int qtyB,
                                      boolean withInvoice, int invoiceStatus) {
        BigDecimal amountA = pA.getWholesalePrice().multiply(BigDecimal.valueOf(qtyA));
        BigDecimal amountB = pB != null && qtyB > 0
                ? pB.getWholesalePrice().multiply(BigDecimal.valueOf(qtyB)) : BigDecimal.ZERO;
        BigDecimal total = amountA.add(amountB);
        BigDecimal freight = new BigDecimal("12.00");

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setStatus(status);
        order.setOrderType(orderType);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(address);
        order.setTotalAmount(total);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFreight(freight);
        order.setPayAmount(total.add(freight));
        order.setInvoiceTitle("某某药店有限公司");
        order.setInvoiceTaxNo("91110000MA01234567");
        order.setInvoiceStatus(withInvoice && invoiceStatus >= 1 ? 1 : 0);
        if (status >= 2) {
            order.setPayType(1);
            order.setPayTime(LocalDateTime.now().minusDays(2));
            order.setTransactionNo("PAY" + orderNo);
        }
        if (status >= 3) {
            order.setLogisticsCompany("顺丰快递");
            order.setLogisticsNo("SF" + orderNo.substring(orderNo.length() - 8));
            order.setShipTime(LocalDateTime.now().minusDays(1));
        }
        if (status == 4) {
            order.setReceiveTime(LocalDateTime.now());
        }
        if (status == 5) {
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason("用户主动取消");
        }
        orderService.save(order);

        saveOrderItem(order.getId(), pA, qtyA);
        if (pB != null && qtyB > 0) {
            saveOrderItem(order.getId(), pB, qtyB);
        }

        if (withInvoice) {
            Invoice invoice = new Invoice();
            invoice.setOrderId(order.getId());
            invoice.setOrderNo(orderNo);
            invoice.setUserId(userId);
            invoice.setMerchantId(merchantId);
            invoice.setInvoiceTitle(order.getInvoiceTitle());
            invoice.setTaxNumber(order.getInvoiceTaxNo());
            invoice.setInvoiceType(2);
            invoice.setInvoiceContent("医药商品");
            invoice.setAmount(order.getPayAmount());
            invoice.setStatus(invoiceStatus);
            invoice.setReceiverName(receiverName);
            invoice.setReceiverPhone(receiverPhone);
            invoice.setReceiverAddress(address);
            if (invoiceStatus >= 1) {
                invoice.setInvoiceNo("INV" + orderNo);
                invoice.setInvoiceTime(LocalDateTime.now().minusDays(1));
            }
            if (invoiceStatus >= 2) {
                invoice.setLogisticsCompany("顺丰快递");
                invoice.setLogisticsNo("SFINV" + orderNo.substring(orderNo.length() - 6));
                invoice.setSendTime(LocalDateTime.now());
            }
            invoiceService.save(invoice);
        }
    }

    private void saveOrderItem(Long orderId, Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(product.getId());
        item.setProductName(product.getProductName());
        item.setProductImage(product.getMainImage());
        item.setPrice(product.getWholesalePrice());
        item.setQuantity(quantity);
        item.setSubtotal(product.getWholesalePrice().multiply(BigDecimal.valueOf(quantity)));
        item.setSpecification(product.getSpecification());
        orderItemService.save(item);
    }
}

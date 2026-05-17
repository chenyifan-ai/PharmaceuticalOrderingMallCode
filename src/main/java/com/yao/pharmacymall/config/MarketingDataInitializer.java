package com.yao.pharmacymall.config;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.pharmacymall.common.ProductImageCatalog;
import com.yao.pharmacymall.entity.HomeBanner;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.entity.ProductPackage;
import com.yao.pharmacymall.entity.SeckillItem;
import com.yao.pharmacymall.mapper.HomeBannerMapper;
import com.yao.pharmacymall.mapper.ProductPackageMapper;
import com.yao.pharmacymall.mapper.SeckillItemMapper;
import com.yao.pharmacymall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Order(2)
public class MarketingDataInitializer implements CommandLineRunner {

    @Autowired
    private SeckillItemMapper seckillItemMapper;
    @Autowired
    private ProductPackageMapper productPackageMapper;
    @Autowired
    private HomeBannerMapper homeBannerMapper;
    @Autowired
    private ProductService productService;

    @Override
    public void run(String... args) {
        if (seckillItemMapper.selectCount(null) > 0) {
            return;
        }
        Map<String, Long> productIds = loadProductIds();
        if (productIds.isEmpty()) {
            log.warn("无商品数据，跳过营销数据初始化");
            return;
        }

        seedBanners();
        seedSeckills(productIds);
        seedPackages(productIds);
        log.info("首页营销数据（轮播/秒杀/套餐）初始化完成");
    }

    private Map<String, Long> loadProductIds() {
        Map<String, Long> map = new HashMap<>();
        for (Product p : productService.list()) {
            map.put(p.getProductName(), p.getId());
        }
        return map;
    }

    private void seedBanners() {
        saveBanner("春季防疫采购季", "OTC常备药低至7折", ProductImageCatalog.banner("spring.svg"),
                "NONE", null, 1);
        saveBanner("限时秒杀", "每日10点开抢 数量有限", ProductImageCatalog.banner("seckill.svg"),
                "SECKILL", "1", 2);
        saveBanner("家庭常备套餐", "一键配齐 省心省钱", ProductImageCatalog.banner("family.svg"),
                "PACKAGE", "1", 3);
    }

    private void saveBanner(String title, String subtitle, String image, String linkType, String linkValue, int sort) {
        HomeBanner b = new HomeBanner();
        b.setTitle(title);
        b.setSubtitle(subtitle);
        b.setImageUrl(image);
        b.setLinkType(linkType);
        b.setLinkValue(linkValue);
        b.setSort(sort);
        b.setStatus(1);
        homeBannerMapper.insert(b);
    }

    private void seedSeckills(Map<String, Long> ids) {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        addSeckill(ids, "布洛芬缓释胶囊", "布洛芬秒杀", new BigDecimal("16.80"), new BigDecimal("22.50"), 200, 156, start, end, 1);
        addSeckill(ids, "连花清瘟胶囊", "连花清瘟秒杀", new BigDecimal("13.90"), new BigDecimal("18.00"), 300, 210, start, end, 2);
        addSeckill(ids, "维生素C咀嚼片", "维C秒杀", new BigDecimal("42.00"), new BigDecimal("58.00"), 150, 88, start, end, 3);
        addSeckill(ids, "藿香正气水", "藿香正气水秒杀", new BigDecimal("7.50"), new BigDecimal("9.90"), 500, 320, start, end, 4);
    }

    private void addSeckill(Map<String, Long> ids, String productName, String title,
                            BigDecimal seckillPrice, BigDecimal originalPrice,
                            int stock, int sold, LocalDateTime start, LocalDateTime end, int sort) {
        Long productId = ids.get(productName);
        if (productId == null) {
            return;
        }
        SeckillItem item = new SeckillItem();
        item.setTitle(title);
        item.setProductId(productId);
        item.setSeckillPrice(seckillPrice);
        item.setOriginalPrice(originalPrice);
        item.setStock(stock);
        item.setSoldCount(sold);
        item.setStartTime(start);
        item.setEndTime(end);
        item.setSort(sort);
        item.setStatus(1);
        seckillItemMapper.insert(item);
    }

    private void seedPackages(Map<String, Long> ids) {
        savePackage("家庭常备防疫套餐", "感冒发热 + 肠胃调理 + 外伤护理",
                ProductImageCatalog.banner("pkg-family.svg"),
                new BigDecimal("68.00"), new BigDecimal("49.90"), 500,
                ids, new String[][]{
                        {"连花清瘟胶囊", "1"}, {"藿香正气水", "1"}, {"创可贴", "1"}
                }, 1);
        savePackage("心脑血管关爱套餐", "慢病常备 批发直供",
                ProductImageCatalog.banner("pkg-heart.svg"),
                new BigDecimal("320.00"), new BigDecimal("259.00"), 200,
                ids, new String[][]{{"电子血压计", "1"}, {"钙维生素D片", "1"}}, 2);
        savePackage("诊所开业 starter 套餐", "抗生素 + 解热镇痛 组合装",
                ProductImageCatalog.banner("pkg-clinic.svg"),
                new BigDecimal("55.00"), new BigDecimal("39.90"), 300,
                ids, new String[][]{{"阿莫西林胶囊", "2"}, {"布洛芬缓释胶囊", "2"}}, 3);
        savePackage("增强免疫力套餐", "维C + 连花清瘟 换季必备",
                ProductImageCatalog.banner("spring.svg"),
                new BigDecimal("78.00"), new BigDecimal("58.90"), 400,
                ids, new String[][]{{"维生素C咀嚼片", "2"}, {"连花清瘟胶囊", "1"}}, 4);
        savePackage("解热消炎组合包", "布洛芬 + 阿莫西林 经典搭配",
                ProductImageCatalog.banner("pkg-clinic.svg"),
                new BigDecimal("48.00"), new BigDecimal("35.80"), 350,
                ids, new String[][]{{"布洛芬缓释胶囊", "2"}, {"阿莫西林胶囊", "1"}}, 5);
        savePackage("夏季常备清凉包", "藿香正气水组合 防暑祛湿",
                ProductImageCatalog.banner("family.svg"),
                new BigDecimal("32.00"), new BigDecimal("24.50"), 600,
                ids, new String[][]{{"藿香正气水", "3"}, {"创可贴", "2"}}, 6);
        savePackage("骨骼健康关怀包", "钙片 + 维C 营养补充",
                ProductImageCatalog.banner("pkg-heart.svg"),
                new BigDecimal("95.00"), new BigDecimal("72.00"), 280,
                ids, new String[][]{{"钙维生素D片", "2"}, {"维生素C咀嚼片", "1"}}, 7);
        savePackage("呼吸道防护组合", "连花清瘟 + 藿香 + 阿莫西林",
                ProductImageCatalog.banner("spring.svg"),
                new BigDecimal("52.00"), new BigDecimal("38.60"), 450,
                ids, new String[][]{
                        {"连花清瘟胶囊", "1"}, {"藿香正气水", "2"}, {"阿莫西林胶囊", "1"}
                }, 8);
        savePackage("门店补货标准包", "热销品一次性配齐",
                ProductImageCatalog.banner("pkg-family.svg"),
                new BigDecimal("88.00"), new BigDecimal("65.00"), 320,
                ids, new String[][]{
                        {"阿莫西林胶囊", "2"}, {"布洛芬缓释胶囊", "2"},
                        {"连花清瘟胶囊", "1"}, {"创可贴", "1"}
                }, 9);
        savePackage("医疗器械入门包", "血压监测 + 外伤护理",
                ProductImageCatalog.banner("seckill.svg"),
                new BigDecimal("198.00"), new BigDecimal("168.00"), 150,
                ids, new String[][]{{"电子血压计", "1"}, {"创可贴", "3"}}, 10);
        savePackage("肠胃调理特惠包", "藿香正气水 + 布洛芬 肠胃感冒",
                ProductImageCatalog.banner("family.svg"),
                new BigDecimal("28.00"), new BigDecimal("19.90"), 500,
                ids, new String[][]{{"藿香正气水", "2"}, {"布洛芬缓释胶囊", "1"}}, 11);
        savePackage("全品类体验装", "8 大品类各 1 件 试采推荐",
                ProductImageCatalog.banner("spring.svg"),
                new BigDecimal("168.00"), new BigDecimal("128.00"), 100,
                ids, new String[][]{
                        {"阿莫西林胶囊", "1"}, {"布洛芬缓释胶囊", "1"},
                        {"连花清瘟胶囊", "1"}, {"藿香正气水", "1"},
                        {"维生素C咀嚼片", "1"}, {"创可贴", "1"},
                        {"钙维生素D片", "1"}, {"电子血压计", "1"}
                }, 12);
    }

    private void savePackage(String name, String subtitle, String banner,
                           BigDecimal original, BigDecimal price, int stock,
                           Map<String, Long> ids, String[][] items, int sort) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] row : items) {
            Long pid = ids.get(row[0]);
            if (pid == null) {
                continue;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("productId", pid);
            m.put("quantity", Integer.parseInt(row[1]));
            m.put("productName", row[0]);
            list.add(m);
        }
        if (list.isEmpty()) {
            return;
        }
        ProductPackage pkg = new ProductPackage();
        pkg.setPackageName(name);
        pkg.setSubtitle(subtitle);
        pkg.setBannerImage(banner);
        pkg.setOriginalPrice(original);
        pkg.setPackagePrice(price);
        pkg.setItems(JSON.toJSONString(list));
        pkg.setStock(stock);
        pkg.setSort(sort);
        pkg.setStatus(1);
        productPackageMapper.insert(pkg);
    }
}

package com.yao.pharmacymall.config;

import com.alibaba.fastjson2.JSON;
import com.yao.pharmacymall.common.ProductImageCatalog;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.entity.ProductPackage;
import com.yao.pharmacymall.mapper.ProductPackageMapper;
import com.yao.pharmacymall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 补全首页活动套餐（按套餐名幂等插入，重启后自动追加新套餐）
 */
@Slf4j
@Component
@Order(25)
public class MarketingPackageSupplementInitializer implements CommandLineRunner {

    @Autowired
    private ProductPackageMapper productPackageMapper;
    @Autowired
    private ProductService productService;

    @Override
    public void run(String... args) {
        Map<String, Long> productIds = loadProductIds();
        if (productIds.isEmpty()) {
            return;
        }
        Set<String> existing = new HashSet<>();
        for (ProductPackage p : productPackageMapper.selectList(null)) {
            existing.add(p.getPackageName());
        }
        int added = 0;
        for (PackageSeed seed : allSeeds()) {
            if (existing.contains(seed.name)) {
                continue;
            }
            if (savePackage(seed, productIds)) {
                added++;
            }
        }
        if (added > 0) {
            log.info("已补全 {} 个活动套餐", added);
        }
    }

    private Map<String, Long> loadProductIds() {
        Map<String, Long> map = new HashMap<>();
        for (Product p : productService.list()) {
            map.put(p.getProductName(), p.getId());
        }
        return map;
    }

    private boolean savePackage(PackageSeed seed, Map<String, Long> ids) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] row : seed.items) {
            Long pid = ids.get(row[0]);
            if (pid == null) {
                return false;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("productId", pid);
            m.put("quantity", Integer.parseInt(row[1]));
            m.put("productName", row[0]);
            list.add(m);
        }
        if (list.isEmpty()) {
            return false;
        }
        ProductPackage pkg = new ProductPackage();
        pkg.setPackageName(seed.name);
        pkg.setSubtitle(seed.subtitle);
        pkg.setBannerImage(seed.banner);
        pkg.setOriginalPrice(seed.original);
        pkg.setPackagePrice(seed.price);
        pkg.setItems(JSON.toJSONString(list));
        pkg.setStock(seed.stock);
        pkg.setSort(seed.sort);
        pkg.setStatus(1);
        productPackageMapper.insert(pkg);
        return true;
    }

    private static List<PackageSeed> allSeeds() {
        List<PackageSeed> list = new ArrayList<>();
        list.add(new PackageSeed("家庭常备防疫套餐", "感冒发热 + 肠胃调理 + 外伤护理",
                ProductImageCatalog.banner("pkg-family.svg"),
                new BigDecimal("68.00"), new BigDecimal("49.90"), 500, 1,
                new String[][]{{"连花清瘟胶囊", "1"}, {"藿香正气水", "1"}, {"创可贴", "1"}}));
        list.add(new PackageSeed("心脑血管关爱套餐", "慢病常备 批发直供",
                ProductImageCatalog.banner("pkg-heart.svg"),
                new BigDecimal("320.00"), new BigDecimal("259.00"), 200, 2,
                new String[][]{{"电子血压计", "1"}, {"钙维生素D片", "1"}}));
        list.add(new PackageSeed("诊所开业 starter 套餐", "抗生素 + 解热镇痛 组合装",
                ProductImageCatalog.banner("pkg-clinic.svg"),
                new BigDecimal("55.00"), new BigDecimal("39.90"), 300, 3,
                new String[][]{{"阿莫西林胶囊", "2"}, {"布洛芬缓释胶囊", "2"}}));
        list.add(new PackageSeed("增强免疫力套餐", "维C + 连花清瘟 换季必备",
                ProductImageCatalog.banner("spring.svg"),
                new BigDecimal("78.00"), new BigDecimal("58.90"), 400, 4,
                new String[][]{{"维生素C咀嚼片", "2"}, {"连花清瘟胶囊", "1"}}));
        list.add(new PackageSeed("解热消炎组合包", "布洛芬 + 阿莫西林 经典搭配",
                ProductImageCatalog.banner("pkg-clinic.svg"),
                new BigDecimal("48.00"), new BigDecimal("35.80"), 350, 5,
                new String[][]{{"布洛芬缓释胶囊", "2"}, {"阿莫西林胶囊", "1"}}));
        list.add(new PackageSeed("夏季常备清凉包", "藿香正气水组合 防暑祛湿",
                ProductImageCatalog.banner("family.svg"),
                new BigDecimal("32.00"), new BigDecimal("24.50"), 600, 6,
                new String[][]{{"藿香正气水", "3"}, {"创可贴", "2"}}));
        list.add(new PackageSeed("骨骼健康关怀包", "钙片 + 维C 营养补充",
                ProductImageCatalog.banner("pkg-heart.svg"),
                new BigDecimal("95.00"), new BigDecimal("72.00"), 280, 7,
                new String[][]{{"钙维生素D片", "2"}, {"维生素C咀嚼片", "1"}}));
        list.add(new PackageSeed("呼吸道防护组合", "连花清瘟 + 藿香 + 阿莫西林",
                ProductImageCatalog.banner("spring.svg"),
                new BigDecimal("52.00"), new BigDecimal("38.60"), 450, 8,
                new String[][]{{"连花清瘟胶囊", "1"}, {"藿香正气水", "2"}, {"阿莫西林胶囊", "1"}}));
        list.add(new PackageSeed("门店补货标准包", "热销品一次性配齐",
                ProductImageCatalog.banner("pkg-family.svg"),
                new BigDecimal("88.00"), new BigDecimal("65.00"), 320, 9,
                new String[][]{
                        {"阿莫西林胶囊", "2"}, {"布洛芬缓释胶囊", "2"},
                        {"连花清瘟胶囊", "1"}, {"创可贴", "1"}
                }));
        list.add(new PackageSeed("医疗器械入门包", "血压监测 + 外伤护理",
                ProductImageCatalog.banner("seckill.svg"),
                new BigDecimal("198.00"), new BigDecimal("168.00"), 150, 10,
                new String[][]{{"电子血压计", "1"}, {"创可贴", "3"}}));
        list.add(new PackageSeed("肠胃调理特惠包", "藿香正气水 + 布洛芬 肠胃感冒",
                ProductImageCatalog.banner("family.svg"),
                new BigDecimal("28.00"), new BigDecimal("19.90"), 500, 11,
                new String[][]{{"藿香正气水", "2"}, {"布洛芬缓释胶囊", "1"}}));
        list.add(new PackageSeed("全品类体验装", "8 大品类各 1 件 试采推荐",
                ProductImageCatalog.banner("spring.svg"),
                new BigDecimal("168.00"), new BigDecimal("128.00"), 100, 12,
                new String[][]{
                        {"阿莫西林胶囊", "1"}, {"布洛芬缓释胶囊", "1"},
                        {"连花清瘟胶囊", "1"}, {"藿香正气水", "1"},
                        {"维生素C咀嚼片", "1"}, {"创可贴", "1"},
                        {"钙维生素D片", "1"}, {"电子血压计", "1"}
                }));
        return list;
    }

    private static final class PackageSeed {
        final String name;
        final String subtitle;
        final String banner;
        final BigDecimal original;
        final BigDecimal price;
        final int stock;
        final int sort;
        final String[][] items;

        PackageSeed(String name, String subtitle, String banner,
                    BigDecimal original, BigDecimal price, int stock, int sort,
                    String[][] items) {
            this.name = name;
            this.subtitle = subtitle;
            this.banner = banner;
            this.original = original;
            this.price = price;
            this.stock = stock;
            this.sort = sort;
            this.items = items;
        }
    }
}

package com.yao.pharmacymall.config;

import com.yao.pharmacymall.common.ProductImageCatalog;
import com.yao.pharmacymall.entity.HomeBanner;
import com.yao.pharmacymall.entity.ProductPackage;
import com.yao.pharmacymall.mapper.HomeBannerMapper;
import com.yao.pharmacymall.mapper.ProductPackageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将轮播/套餐横幅中的失效外链（如 placehold.co）同步为本地静态图
 */
@Slf4j
@Component
@Order(3)
public class MarketingImageSyncInitializer implements CommandLineRunner {

    @Autowired
    private HomeBannerMapper homeBannerMapper;
    @Autowired
    private ProductPackageMapper productPackageMapper;

    @Override
    public void run(String... args) {
        int banners = syncBanners();
        int packages = syncPackages();
        if (banners + packages > 0) {
            log.info("已修复营销图片：轮播 {} 条，套餐 {} 条", banners, packages);
        }
    }

    private int syncBanners() {
        Map<String, String> byTitle = new LinkedHashMap<>();
        byTitle.put("春季防疫采购季", ProductImageCatalog.banner("spring.svg"));
        byTitle.put("限时秒杀", ProductImageCatalog.banner("seckill.svg"));
        byTitle.put("家庭常备套餐", ProductImageCatalog.banner("family.svg"));

        int n = 0;
        for (HomeBanner b : homeBannerMapper.selectList(null)) {
            String target = byTitle.get(b.getTitle());
            if (target != null && needsFix(b.getImageUrl())) {
                b.setImageUrl(target);
                homeBannerMapper.updateById(b);
                n++;
            }
        }
        return n;
    }

    private int syncPackages() {
        Map<String, String> byName = new LinkedHashMap<>();
        byName.put("家庭常备防疫套餐", ProductImageCatalog.banner("pkg-family.svg"));
        byName.put("心脑血管关爱套餐", ProductImageCatalog.banner("pkg-heart.svg"));
        byName.put("诊所开业 starter 套餐", ProductImageCatalog.banner("pkg-clinic.svg"));
        byName.put("增强免疫力套餐", ProductImageCatalog.banner("spring.svg"));
        byName.put("解热消炎组合包", ProductImageCatalog.banner("pkg-clinic.svg"));
        byName.put("夏季常备清凉包", ProductImageCatalog.banner("family.svg"));
        byName.put("骨骼健康关怀包", ProductImageCatalog.banner("pkg-heart.svg"));
        byName.put("呼吸道防护组合", ProductImageCatalog.banner("spring.svg"));
        byName.put("门店补货标准包", ProductImageCatalog.banner("pkg-family.svg"));
        byName.put("医疗器械入门包", ProductImageCatalog.banner("seckill.svg"));
        byName.put("肠胃调理特惠包", ProductImageCatalog.banner("family.svg"));
        byName.put("全品类体验装", ProductImageCatalog.banner("spring.svg"));

        int n = 0;
        for (ProductPackage p : productPackageMapper.selectList(null)) {
            String target = byName.get(p.getPackageName());
            if (target != null && needsFix(p.getBannerImage())) {
                p.setBannerImage(target);
                productPackageMapper.updateById(p);
                n++;
            }
        }
        return n;
    }

    private static boolean needsFix(String url) {
        if (url == null || url.isBlank()) {
            return true;
        }
        String u = url.trim().toLowerCase();
        return u.contains("placehold") || !u.startsWith("/images/");
    }
}

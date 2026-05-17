package com.yao.pharmacymall.config;

import com.yao.pharmacymall.common.ProductImageCatalog;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 为演示商品同步品类实拍图（仅覆盖占位/外链/SVG，不覆盖 B 端已上传图片）
 */
@Slf4j
@Component
@Order(1)
public class ProductImageInitializer implements CommandLineRunner {

    @Autowired
    private ProductService productService;

    @Override
    public void run(String... args) {
        int updated = 0;
        for (Product product : productService.list()) {
            ProductImageCatalog.ImageSet set = ProductImageCatalog.getByProductName(product.getProductName());
            if (set == null) {
                continue;
            }
            if (!ProductImageCatalog.shouldOverwrite(product.getMainImage())) {
                continue;
            }
            productService.lambdaUpdate()
                    .eq(Product::getId, product.getId())
                    .set(Product::getMainImage, set.main)
                    .set(Product::getImages, set.imagesJson())
                    .set(Product::getDetailImages, set.detailImagesJson())
                    .update();
            updated++;
        }
        if (updated > 0) {
            log.info("已同步 {} 个商品的品类实拍图", updated);
        }
    }
}

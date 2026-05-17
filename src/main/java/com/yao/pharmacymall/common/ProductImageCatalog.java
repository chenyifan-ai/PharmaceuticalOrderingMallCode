package com.yao.pharmacymall.common;

import com.alibaba.fastjson2.JSON;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 演示商品图片（本地实拍 JPG，写入数据库后由前端 API 读取；B 端上传的非占位图不会被覆盖）
 */
public final class ProductImageCatalog {

    private ProductImageCatalog() {
    }

    public static final class ImageSet {
        public final String main;
        public final List<String> gallery;
        public final List<String> detail;

        public ImageSet(String main) {
            this.main = main;
            this.gallery = Collections.singletonList(main);
            this.detail = Collections.singletonList(main);
        }

        public String imagesJson() {
            return JSON.toJSONString(gallery);
        }

        public String detailImagesJson() {
            return JSON.toJSONString(detail);
        }
    }

    private static final Map<String, ImageSet> BY_NAME = new LinkedHashMap<>();

    static {
        BY_NAME.put("阿莫西林胶囊", new ImageSet("/images/products/amoxicillin.jpg"));
        BY_NAME.put("布洛芬缓释胶囊", new ImageSet("/images/products/ibuprofen.jpg"));
        BY_NAME.put("连花清瘟胶囊", new ImageSet("/images/products/lianhua.jpg"));
        BY_NAME.put("电子血压计", new ImageSet("/images/products/bp-monitor.jpg"));
        BY_NAME.put("维生素C咀嚼片", new ImageSet("/images/products/vitamin-c.jpg"));
        BY_NAME.put("藿香正气水", new ImageSet("/images/products/huoxiang.jpg"));
        BY_NAME.put("创可贴", new ImageSet("/images/products/bandage.jpg"));
        BY_NAME.put("钙维生素D片", new ImageSet("/images/products/calcium.jpg"));
    }

    public static ImageSet getByProductName(String productName) {
        return BY_NAME.get(productName);
    }

    public static String banner(String filename) {
        return "/images/banners/" + filename;
    }

    /** 是否应被种子数据覆盖（保留 B 端上传的真实图） */
    public static boolean shouldOverwrite(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return true;
        }
        String u = imageUrl.trim().toLowerCase();
        return u.contains("placehold")
                || u.endsWith(".svg")
                || u.contains("unsplash.com");
    }
}

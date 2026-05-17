package com.yao.pharmacymall.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.pharmacymall.dto.*;
import com.yao.pharmacymall.entity.HomeBanner;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.entity.ProductPackage;
import com.yao.pharmacymall.entity.SeckillItem;
import com.yao.pharmacymall.mapper.HomeBannerMapper;
import com.yao.pharmacymall.mapper.ProductPackageMapper;
import com.yao.pharmacymall.mapper.SeckillItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CHomeService {

    @Autowired
    private HomeBannerMapper homeBannerMapper;
    @Autowired
    private SeckillItemMapper seckillItemMapper;
    @Autowired
    private ProductPackageMapper productPackageMapper;
    @Autowired
    private ProductService productService;

    public CHomeVO getHomeData() {
        CHomeVO vo = new CHomeVO();
        vo.setBanners(listActiveBanners());
        vo.setSeckills(listActiveSeckills());
        vo.setPackages(listActivePackages());
        return vo;
    }

    public ProductPackageVO getPackageDetail(Long id) {
        ProductPackage pkg = productPackageMapper.selectById(id);
        if (pkg == null || pkg.getStatus() == null || pkg.getStatus() != 1) {
            return null;
        }
        return toPackageVO(pkg);
    }

    private List<HomeBanner> listActiveBanners() {
        LambdaQueryWrapper<HomeBanner> w = new LambdaQueryWrapper<>();
        w.eq(HomeBanner::getStatus, 1).orderByAsc(HomeBanner::getSort);
        return homeBannerMapper.selectList(w);
    }

    private List<SeckillItemVO> listActiveSeckills() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SeckillItem> w = new LambdaQueryWrapper<>();
        w.eq(SeckillItem::getStatus, 1)
                .le(SeckillItem::getStartTime, now)
                .ge(SeckillItem::getEndTime, now)
                .orderByAsc(SeckillItem::getSort);
        return seckillItemMapper.selectList(w).stream()
                .map(this::toSeckillVO)
                .collect(Collectors.toList());
    }

    private List<ProductPackageVO> listActivePackages() {
        LambdaQueryWrapper<ProductPackage> w = new LambdaQueryWrapper<>();
        w.eq(ProductPackage::getStatus, 1).orderByAsc(ProductPackage::getSort);
        return productPackageMapper.selectList(w).stream()
                .map(this::toPackageVO)
                .collect(Collectors.toList());
    }

    private SeckillItemVO toSeckillVO(SeckillItem item) {
        SeckillItemVO vo = new SeckillItemVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setProductId(item.getProductId());
        vo.setSeckillPrice(item.getSeckillPrice());
        vo.setOriginalPrice(item.getOriginalPrice());
        vo.setStock(item.getStock());
        vo.setSoldCount(item.getSoldCount());
        vo.setEndTime(item.getEndTime());
        int total = (item.getStock() != null ? item.getStock() : 0)
                + (item.getSoldCount() != null ? item.getSoldCount() : 0);
        if (total > 0) {
            vo.setProgressPercent(Math.min(99,
                    (item.getSoldCount() != null ? item.getSoldCount() : 0) * 100 / total));
        } else {
            vo.setProgressPercent(0);
        }
        Product product = productService.getById(item.getProductId());
        if (product != null && product.getStatus() != null && product.getStatus() == 1) {
            vo.setProduct(product);
        }
        return vo;
    }

    private ProductPackageVO toPackageVO(ProductPackage pkg) {
        ProductPackageVO vo = new ProductPackageVO();
        vo.setId(pkg.getId());
        vo.setPackageName(pkg.getPackageName());
        vo.setSubtitle(pkg.getSubtitle());
        vo.setBannerImage(pkg.getBannerImage());
        vo.setOriginalPrice(pkg.getOriginalPrice());
        vo.setPackagePrice(pkg.getPackagePrice());
        vo.setStock(pkg.getStock());
        if (pkg.getOriginalPrice() != null && pkg.getPackagePrice() != null
                && pkg.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal off = pkg.getOriginalPrice().subtract(pkg.getPackagePrice())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(pkg.getOriginalPrice(), 0, RoundingMode.HALF_UP);
            vo.setDiscountPercent(off.intValue());
        }
        vo.setItems(enrichPackageItems(pkg.getItems()));
        return vo;
    }

    private List<PackageItemDTO> enrichPackageItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isBlank()) {
            return new ArrayList<>();
        }
        List<PackageItemDTO> items = JSON.parseObject(itemsJson, new TypeReference<List<PackageItemDTO>>() {});
        if (items == null) {
            return new ArrayList<>();
        }
        for (PackageItemDTO item : items) {
            if (item.getProductId() == null) {
                continue;
            }
            Product p = productService.getById(item.getProductId());
            if (p == null) {
                continue;
            }
            if (item.getProductName() == null) {
                item.setProductName(p.getProductName());
            }
            if (item.getSpecification() == null) {
                item.setSpecification(p.getSpecification());
            }
            if (item.getImage() == null) {
                item.setImage(p.getMainImage());
            }
        }
        return items;
    }
}

package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.ProductQueryRequest;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 商品控制器(C端)
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表(支持搜索和筛选)
     */
    @GetMapping("/list")
    public Result<PageResult<Product>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String prescriptionType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        ProductQueryRequest request = new ProductQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setCategoryId(categoryId);
        request.setPrescriptionType(prescriptionType);
        request.setKeyword(keyword);
        request.setBrand(brand);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);

        PageResult<Product> result = productService.getProductListEnhanced(request);
        return Result.success(result);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        return Result.success(product);
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public Result<PageResult<Product>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        ProductQueryRequest request = new ProductQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);

        PageResult<Product> result = productService.getProductListEnhanced(request);
        return Result.success(result);
    }
}

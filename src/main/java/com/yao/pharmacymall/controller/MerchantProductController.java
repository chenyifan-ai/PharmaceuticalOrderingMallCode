package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.service.ProductService;
import com.yao.pharmacymall.util.MerchantAuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 供应商商品管理控制器(B端)
 */
@RestController
@RequestMapping("/api/merchant/product")
public class MerchantProductController {

    @Autowired
    private ProductService productService;

    /**
     * 发布商品
     */
    @PostMapping("/publish")
    public Result<?> publishProduct(HttpServletRequest request, @RequestBody Product product) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        product.setSupplierId(merchantId);
        productService.publishProduct(product, merchantId);
        return Result.success("商品发布成功，等待审核", null);
    }

    /**
     * 更新商品
     */
    @PutMapping("/update")
    public Result<?> updateProduct(HttpServletRequest request, @RequestBody Product product) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        productService.updateProduct(product, merchantId);
        return Result.success("更新成功", null);
    }

    /**
     * 下架商品
     */
    @PostMapping("/offline/{id}")
    public Result<?> offlineProduct(HttpServletRequest request, @PathVariable Long id) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        productService.offlineProduct(id, merchantId);
        return Result.success("下架成功", null);
    }

    /**
     * 查询我的商品列表
     */
    @GetMapping("/list")
    public Result<PageResult<Product>> getMyProducts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        PageResult<Product> result = productService.getSupplierProducts(merchantId, page, pageSize, status);
        return Result.success(result);
    }
}
package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员商品控制器
 */
@RestController
@RequestMapping(value = "/api/admin/product", produces = "application/json;charset=UTF-8")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表（管理员）
     */
    @GetMapping("/list")
    public Result<PageResult<Product>> getProductList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        PageResult<Product> result = productService.getAdminProductList(keyword, status, page, pageSize);
        return Result.success(result);
    }

    /**
     * 审核商品（管理员）
     */
    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }

    @PostMapping("/audit/{id}")
    public Result<Boolean> auditProduct(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        productService.auditProduct(id, status, remark);
        return Result.success(true);
    }

    /**
     * 违规/主动下架商品
     */
    @PostMapping("/offline/{id}")
    public Result<Boolean> offlineProduct(@PathVariable Long id) {
        productService.adminOfflineProduct(id);
        return Result.success("下架成功", true);
    }

    /**
     * 管理员上架商品
     */
    @PostMapping("/online/{id}")
    public Result<Boolean> onlineProduct(@PathVariable Long id) {
        productService.adminOnlineProduct(id);
        return Result.success("上架成功", true);
    }

    /**
     * 管理员新增药品
     */
    @PostMapping
    public Result<Product> createProduct(@RequestBody Product product) {
        Product created = productService.adminCreateProduct(product);
        return Result.success("添加成功", created);
    }

    /**
     * 管理员修改药品
     */
    @PutMapping("/{id}")
    public Result<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.adminUpdateProduct(product);
        return Result.success("修改成功", null);
    }

    /**
     * 管理员删除药品（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        productService.adminDeleteProduct(id);
        return Result.success("删除成功", null);
    }
}

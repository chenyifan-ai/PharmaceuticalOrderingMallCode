package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.ProductQueryRequest;
import com.yao.pharmacymall.entity.Category;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.service.CategoryService;
import com.yao.pharmacymall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端商品控制器 - 供采购方使用的商品相关接口
 */
@RestController
@RequestMapping("/api/c/product")
public class CProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

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
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String dosageForm,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDir) {

        ProductQueryRequest request = new ProductQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setCategoryId(categoryId);
        request.setPrescriptionType(prescriptionType);
        request.setKeyword(keyword);
        request.setBrand(brand);
        request.setBrandId(brandId);
        request.setDosageForm(dosageForm);
        request.setManufacturer(manufacturer);
        request.setMinPrice(minPrice != null ? java.math.BigDecimal.valueOf(minPrice) : null);
        request.setMaxPrice(maxPrice != null ? java.math.BigDecimal.valueOf(maxPrice) : null);
        request.setOrderBy(orderBy);
        request.setOrderDir(orderDir);

        PageResult<Product> result = productService.getProductListC(request);
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
     * 获取商品分类列表
     */
    @GetMapping("/categories")
    public Result<List<Category>> getCategories(@RequestParam(required = false) Long parentId) {
        List<Category> categories = categoryService.getListByParentId(parentId);
        return Result.success(categories);
    }

    /**
     * 获取分类树形结构
     */
    @GetMapping("/category-tree")
    public Result<List<Category>> getCategoryTree() {
        List<Category> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    /**
     * 获取热门搜索关键词
     */
    @GetMapping("/hot-searches")
    public Result<List<String>> getHotSearches() {
        return Result.success(productService.getHotSearchKeywords());
    }

    /**
     * 获取搜索建议
     */
    @GetMapping("/search-suggestions")
    public Result<List<String>> getSearchSuggestions(@RequestParam String keyword) {
        return Result.success(List.of());
    }

    /**
     * 获取推荐商品
     */
    @GetMapping("/recommend")
    public Result<PageResult<Product>> getRecommendProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<Product> result = productService.getRecommendProducts(page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/hot")
    public Result<PageResult<Product>> getHotProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<Product> result = productService.getHotProducts(page, pageSize);
        return Result.success(result);
    }
}
package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.TierPrice;
import com.yao.pharmacymall.service.ProductService;
import com.yao.pharmacymall.util.TierPriceCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 阶梯价格控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/product/tier-price")
public class TierPriceController {

    @Autowired
    private ProductService productService;

    /**
     * 设置商品的阶梯价格
     */
    @PostMapping("/set/{productId}")
    public Result<?> setTierPrices(
            @PathVariable Long productId,
            @RequestBody List<TierPrice> tierPrices) {

        log.info("设置商品阶梯价格: productId={}, tierPrices={}", productId, tierPrices);

        // 转换为JSON
        String tierPricesJson = TierPriceCalculator.toJson(tierPrices);

        // 验证格式
        if (!TierPriceCalculator.isValid(tierPricesJson)) {
            return Result.error("阶梯价格配置无效");
        }

        // 保存
        productService.setTierPrices(productId, tierPricesJson);

        return Result.success("设置成功");
    }

    /**
     * 获取商品的阶梯价格
     */
    @GetMapping("/get/{productId}")
    public Result<String> getTierPrices(@PathVariable Long productId) {
        String tierPricesJson = productService.getTierPrices(productId);
        return Result.success(tierPricesJson);
    }

    /**
     * 根据购买数量计算价格
     */
    @PostMapping("/calculate")
    public Result<Map<String, Object>> calculatePrice(@RequestBody Map<String, Object> params) {
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());

        BigDecimal unitPrice = productService.calculatePriceWithTier(productId, quantity);
        BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));

        Map<String, Object> result = Map.of(
            "unitPrice", unitPrice,
            "quantity", quantity,
            "totalAmount", totalAmount
        );

        return Result.success(result);
    }

    /**
     * 验证阶梯价格配置
     */
    @PostMapping("/validate")
    public Result<Boolean> validateTierPrices(@RequestBody List<TierPrice> tierPrices) {
        String tierPricesJson = TierPriceCalculator.toJson(tierPrices);
        boolean isValid = TierPriceCalculator.isValid(tierPricesJson);
        return Result.success(isValid);
    }
}

package com.yao.pharmacymall.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yao.pharmacymall.dto.TierPrice;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 阶梯价格计算器
 */
@Slf4j
public class TierPriceCalculator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据购买数量计算阶梯价格
     *
     * @param tierPricesJson 阶梯价格JSON字符串
     * @param quantity       购买数量
     * @param defaultPrice   默认价格(当没有配置阶梯价格时使用)
     * @return 计算后的单价
     */
    public static BigDecimal calculatePrice(String tierPricesJson, Integer quantity, BigDecimal defaultPrice) {
        if (quantity == null || quantity <= 0) {
            log.warn("购买数量无效: {}", quantity);
            return defaultPrice;
        }

        // 如果没有配置阶梯价格,返回默认价格
        if (tierPricesJson == null || tierPricesJson.trim().isEmpty()) {
            return defaultPrice;
        }

        try {
            // 解析JSON格式的阶梯价格
            List<TierPrice> tierPrices = parseTierPrices(tierPricesJson);

            if (tierPrices == null || tierPrices.isEmpty()) {
                return defaultPrice;
            }

            // 按最小数量排序
            tierPrices.sort(Comparator.comparingInt(TierPrice::getMinQuantity));

            // 查找匹配的阶梯价格
            for (TierPrice tier : tierPrices) {
                if (tier.isInRange(quantity)) {
                    log.debug("匹配阶梯价格: quantity={}, price={}", quantity, tier.getPrice());
                    return tier.getPrice();
                }
            }

            // 如果没有找到匹配的阶梯,使用最高阶梯的价格或默认价格
            TierPrice highestTier = tierPrices.get(tierPrices.size() - 1);
            if (quantity >= highestTier.getMinQuantity()) {
                log.debug("使用最高阶梯价格: quantity={}, price={}", quantity, highestTier.getPrice());
                return highestTier.getPrice();
            }

            return defaultPrice;

        } catch (Exception e) {
            log.error("计算阶梯价格失败: quantity={}, error={}", quantity, e.getMessage(), e);
            return defaultPrice;
        }
    }

    /**
     * 计算订单总金额(考虑阶梯价格)
     *
     * @param tierPricesJson 阶梯价格JSON字符串
     * @param quantity       购买数量
     * @param defaultPrice   默认价格
     * @return 总金额
     */
    public static BigDecimal calculateTotalAmount(String tierPricesJson, Integer quantity, BigDecimal defaultPrice) {
        BigDecimal unitPrice = calculatePrice(tierPricesJson, quantity, defaultPrice);
        return unitPrice.multiply(new BigDecimal(quantity));
    }

    /**
     * 解析阶梯价格JSON
     */
    private static List<TierPrice> parseTierPrices(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<TierPrice>>() {
            });
        } catch (Exception e) {
            log.error("解析阶梯价格JSON失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 将阶梯价格列表转换为JSON字符串
     */
    public static String toJson(List<TierPrice> tierPrices) {
        try {
            return objectMapper.writeValueAsString(tierPrices);
        } catch (Exception e) {
            log.error("转换阶梯价格为JSON失败: {}", e.getMessage());
            return "[]";
        }
    }

    /**
     * 验证阶梯价格配置是否有效
     */
    public static boolean isValid(String tierPricesJson) {
        if (tierPricesJson == null || tierPricesJson.trim().isEmpty()) {
            return true; // 空配置也是有效的
        }

        try {
            List<TierPrice> tierPrices = parseTierPrices(tierPricesJson);

            if (tierPrices.isEmpty()) {
                return true;
            }

            // 验证每个阶梯的有效性
            for (TierPrice tier : tierPrices) {
                if (tier.getMinQuantity() == null || tier.getMinQuantity() <= 0) {
                    return false;
                }
                if (tier.getPrice() == null || tier.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
                if (tier.getMaxQuantity() != null && tier.getMaxQuantity() <= tier.getMinQuantity()) {
                    return false;
                }
            }

            // 验证阶梯之间没有重叠
            tierPrices.sort(Comparator.comparingInt(TierPrice::getMinQuantity));
            for (int i = 1; i < tierPrices.size(); i++) {
                TierPrice prev = tierPrices.get(i - 1);
                TierPrice curr = tierPrices.get(i);

                if (prev.getMaxQuantity() != null && prev.getMaxQuantity() > curr.getMinQuantity()) {
                    return false; // 阶梯有重叠
                }
            }

            return true;

        } catch (Exception e) {
            log.error("验证阶梯价格失败: {}", e.getMessage());
            return false;
        }
    }
}

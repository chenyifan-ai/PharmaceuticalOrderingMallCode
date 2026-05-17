package com.yao.pharmacymall.util;

import com.yao.pharmacymall.dto.TierPrice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 阶梯价格计算器测试类
 */
public class TierPriceCalculatorTest {

    @Test
    void testCalculatePrice_NoTierPrices() {
        // 没有配置阶梯价格,应该返回默认价格
        BigDecimal result = TierPriceCalculator.calculatePrice(null, 10, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void testCalculatePrice_EmptyTierPrices() {
        // 空的阶梯价格配置
        BigDecimal result = TierPriceCalculator.calculatePrice("", 10, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void testCalculatePrice_SingleTier() {
        // 单个阶梯: 购买10个以上,单价90
        List<TierPrice> tiers = new ArrayList<>();
        tiers.add(new TierPrice(10, null, new BigDecimal("90.00")));

        String json = TierPriceCalculator.toJson(tiers);

        // 购买5个,不满足阶梯条件
        BigDecimal price1 = TierPriceCalculator.calculatePrice(json, 5, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), price1);

        // 购买10个,满足阶梯条件
        BigDecimal price2 = TierPriceCalculator.calculatePrice(json, 10, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("90.00"), price2);

        // 购买20个,满足阶梯条件
        BigDecimal price3 = TierPriceCalculator.calculatePrice(json, 20, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("90.00"), price3);
    }

    @Test
    void testCalculatePrice_MultipleTiers() {
        // 多个阶梯
        List<TierPrice> tiers = new ArrayList<>();
        tiers.add(new TierPrice(1, 10, new BigDecimal("100.00")));   // 1-9个: 100元
        tiers.add(new TierPrice(10, 50, new BigDecimal("90.00")));   // 10-49个: 90元
        tiers.add(new TierPrice(50, null, new BigDecimal("80.00"))); // 50个以上: 80元

        String json = TierPriceCalculator.toJson(tiers);

        // 购买5个
        BigDecimal price1 = TierPriceCalculator.calculatePrice(json, 5, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), price1);

        // 购买10个
        BigDecimal price2 = TierPriceCalculator.calculatePrice(json, 10, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("90.00"), price2);

        // 购买49个
        BigDecimal price3 = TierPriceCalculator.calculatePrice(json, 49, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("90.00"), price3);

        // 购买50个
        BigDecimal price4 = TierPriceCalculator.calculatePrice(json, 50, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("80.00"), price4);

        // 购买100个
        BigDecimal price5 = TierPriceCalculator.calculatePrice(json, 100, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("80.00"), price5);
    }

    @Test
    void testCalculateTotalAmount() {
        List<TierPrice> tiers = new ArrayList<>();
        tiers.add(new TierPrice(1, 10, new BigDecimal("100.00")));
        tiers.add(new TierPrice(10, null, new BigDecimal("90.00")));

        String json = TierPriceCalculator.toJson(tiers);

        // 购买5个,总价500
        BigDecimal total1 = TierPriceCalculator.calculateTotalAmount(json, 5, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("500.00"), total1);

        // 购买10个,总价900
        BigDecimal total2 = TierPriceCalculator.calculateTotalAmount(json, 10, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("900.00"), total2);
    }

    @Test
    void testIsValid_ValidConfiguration() {
        List<TierPrice> tiers = new ArrayList<>();
        tiers.add(new TierPrice(1, 10, new BigDecimal("100.00")));
        tiers.add(new TierPrice(10, 50, new BigDecimal("90.00")));
        tiers.add(new TierPrice(50, null, new BigDecimal("80.00")));

        String json = TierPriceCalculator.toJson(tiers);
        assertTrue(TierPriceCalculator.isValid(json));
    }

    @Test
    void testIsValid_InvalidMinQuantity() {
        List<TierPrice> tiers = new ArrayList<>();
        tiers.add(new TierPrice(0, 10, new BigDecimal("100.00"))); // 最小数量为0,无效

        String json = TierPriceCalculator.toJson(tiers);
        assertFalse(TierPriceCalculator.isValid(json));
    }

    @Test
    void testIsValid_InvalidPrice() {
        List<TierPrice> tiers = new ArrayList<>();
        tiers.add(new TierPrice(1, 10, new BigDecimal("-10.00"))); // 负数价格,无效

        String json = TierPriceCalculator.toJson(tiers);
        assertFalse(TierPriceCalculator.isValid(json));
    }

    @Test
    void testIsValid_OverlappingTiers() {
        List<TierPrice> tiers = new ArrayList<>();
        tiers.add(new TierPrice(1, 20, new BigDecimal("100.00")));
        tiers.add(new TierPrice(10, 50, new BigDecimal("90.00"))); // 与上一个阶梯重叠

        String json = TierPriceCalculator.toJson(tiers);
        assertFalse(TierPriceCalculator.isValid(json));
    }

    @Test
    void testIsValid_MaxLessThanMin() {
        List<TierPrice> tiers = new ArrayList<>();
        tiers.add(new TierPrice(10, 5, new BigDecimal("100.00"))); // 最大值小于最小值

        String json = TierPriceCalculator.toJson(tiers);
        assertFalse(TierPriceCalculator.isValid(json));
    }

    @Test
    void testToJsonAndParse() {
        List<TierPrice> original = new ArrayList<>();
        original.add(new TierPrice(1, 10, new BigDecimal("100.00")));
        original.add(new TierPrice(10, null, new BigDecimal("90.00")));

        String json = TierPriceCalculator.toJson(original);
        assertNotNull(json);
        assertTrue(json.contains("minQuantity"));
        assertTrue(json.contains("price"));
    }

    @Test
    void testCalculatePrice_InvalidQuantity() {
        // 数量为0或负数,应该返回默认价格
        BigDecimal price1 = TierPriceCalculator.calculatePrice("[]", 0, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), price1);

        BigDecimal price2 = TierPriceCalculator.calculatePrice("[]", -5, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), price2);
    }
}

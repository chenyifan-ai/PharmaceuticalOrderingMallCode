package com.yao.pharmacymall.service;

import com.yao.pharmacymall.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 库存并发扣减测试类
 * 测试乐观锁在高并发场景下的表现
 */
@SpringBootTest
public class ProductStockConcurrencyTest {

    @Autowired
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 创建测试商品,库存100
        testProduct = new Product();
        testProduct.setProductName("测试商品-并发测试");
        testProduct.setGenericName("测试通用名");
        testProduct.setPrescriptionType("OTC");
        testProduct.setCategoryId(1L);
        testProduct.setBrand("测试品牌");
        testProduct.setSpecification("10片/盒");
        testProduct.setManufacturer("测试厂家");
        testProduct.setApprovalNumber("国药准字H20240001");
        testProduct.setStock(100);
        testProduct.setSales(0);
        testProduct.setMinOrderQuantity(1);
        testProduct.setMaxOrderQuantity(10);
        testProduct.setWholesalePrice(new BigDecimal("50.00"));
        testProduct.setMarketPrice(new BigDecimal("80.00"));
        testProduct.setStatus(1);
        testProduct.setAuditStatus(1);
        testProduct.setSupplierId(1L);
        testProduct.setVersion(0); // 初始化版本号

        productService.save(testProduct);
    }

    @Test
    void testConcurrentStockDecrease() throws InterruptedException {
        int threadCount = 10; // 10个并发线程
        int decreasePerThread = 5; // 每个线程扣减5个
        int expectedTotalDecrease = threadCount * decreasePerThread; // 预期总共扣减50个

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 启动10个线程同时扣减库存
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean success = productService.decreaseStock(testProduct.getId(), decreasePerThread);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        latch.await();
        executor.shutdown();

        // 验证结果
        Product updatedProduct = productService.getById(testProduct.getId());
        int actualDecrease = 100 - updatedProduct.getStock();

        System.out.println("并发测试结果:");
        System.out.println("  初始库存: 100");
        System.out.println("  线程数: " + threadCount);
        System.out.println("  每线程扣减量: " + decreasePerThread);
        System.out.println("  成功次数: " + successCount.get());
        System.out.println("  失败次数: " + failCount.get());
        System.out.println("  实际扣减总量: " + actualDecrease);
        System.out.println("  剩余库存: " + updatedProduct.getStock());
        System.out.println("  版本号: " + updatedProduct.getVersion());

        // 所有扣减都应该成功(因为总扣减量50 < 初始库存100)
        assertEquals(threadCount, successCount.get(), "所有线程都应该成功扣减");
        assertEquals(0, failCount.get(), "不应该有失败的扣减");
        assertEquals(expectedTotalDecrease, actualDecrease, "总扣减量应该等于预期值");
        assertEquals(50, updatedProduct.getStock(), "剩余库存应该是50");
    }

    @Test
    void testStockExhaustion() throws InterruptedException {
        int threadCount = 25; // 25个并发线程
        int decreasePerThread = 5; // 每个线程扣减5个
        // 总需求125 > 库存100,应该有部分线程失败

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean success = productService.decreaseStock(testProduct.getId(), decreasePerThread);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Product updatedProduct = productService.getById(testProduct.getId());

        System.out.println("\n库存耗尽测试:");
        System.out.println("  初始库存: 100");
        System.out.println("  线程数: " + threadCount);
        System.out.println("  每线程扣减量: " + decreasePerThread);
        System.out.println("  总需求: " + (threadCount * decreasePerThread));
        System.out.println("  成功次数: " + successCount.get());
        System.out.println("  失败次数: " + failCount.get());
        System.out.println("  剩余库存: " + updatedProduct.getStock());

        // 最多只能成功20次(100 / 5 = 20)
        assertTrue(successCount.get() <= 20, "成功次数不能超过20");
        assertTrue(failCount.get() >= 5, "至少应该有5次失败");
        assertTrue(updatedProduct.getStock() >= 0, "库存不能为负数");
    }

    @Test
    void testBatchDecreaseStock() {
        // 创建第二个测试商品
        Product product2 = new Product();
        product2.setProductName("测试商品2");
        product2.setGenericName("测试通用名2");
        product2.setPrescriptionType("OTC");
        product2.setCategoryId(1L);
        product2.setBrand("测试品牌");
        product2.setSpecification("20片/盒");
        product2.setManufacturer("测试厂家");
        product2.setApprovalNumber("国药准字H20240002");
        product2.setStock(50);
        product2.setSales(0);
        product2.setMinOrderQuantity(1);
        product2.setMaxOrderQuantity(10);
        product2.setWholesalePrice(new BigDecimal("30.00"));
        product2.setMarketPrice(new BigDecimal("50.00"));
        product2.setStatus(1);
        product2.setAuditStatus(1);
        product2.setSupplierId(1L);
        product2.setVersion(0);
        productService.save(product2);

        // 批量扣减两个商品的库存
        java.util.List<Long> productIds = java.util.Arrays.asList(testProduct.getId(), product2.getId());
        java.util.List<Integer> quantities = java.util.Arrays.asList(10, 5);

        boolean success = productService.batchDecreaseStock(productIds, quantities);

        assertTrue(success, "批量扣减应该成功");

        Product updated1 = productService.getById(testProduct.getId());
        Product updated2 = productService.getById(product2.getId());

        assertEquals(90, updated1.getStock(), "商品1库存应该是90");
        assertEquals(45, updated2.getStock(), "商品2库存应该是45");
    }

    @Test
    void testBatchDecreaseStockWithInsufficientStock() {
        // 尝试扣减超过库存的数量
        java.util.List<Long> productIds = java.util.Arrays.asList(testProduct.getId());
        java.util.List<Integer> quantities = java.util.Arrays.asList(150); // 超过库存100

        assertThrows(RuntimeException.class, () -> {
            productService.batchDecreaseStock(productIds, quantities);
        }, "库存不足应该抛出异常");
    }
}

package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.dto.StockAdjustRequest;
import com.yao.pharmacymall.dto.StockInboundRequest;
import com.yao.pharmacymall.dto.StockListVO;
import com.yao.pharmacymall.dto.StockSummaryVO;
import com.yao.pharmacymall.dto.StockWarningRequest;
import com.yao.pharmacymall.entity.OrderItem;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.entity.ProductBatch;
import com.yao.pharmacymall.entity.ProductStock;
import com.yao.pharmacymall.entity.StockLog;
import com.yao.pharmacymall.mapper.ProductBatchMapper;
import com.yao.pharmacymall.mapper.StockLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockManageService {

    private static final int DEFAULT_WARNING = 10;

    private final ProductService productService;
    private final ProductStockService productStockService;
    private final StockLogMapper stockLogMapper;
    private final ProductBatchMapper productBatchMapper;
    private final OrderItemService orderItemService;

    public StockSummaryVO getSummary(Long supplierId) {
        LambdaQueryWrapper<Product> wrapper = buildProductWrapper(supplierId, null, null, null);
        List<Product> products = productService.list(wrapper);
        Map<Long, ProductStock> stockMap = loadStockMap(products);

        StockSummaryVO vo = new StockSummaryVO();
        vo.setTotalSku((long) products.size());
        long low = 0;
        long out = 0;
        long totalQty = 0;
        for (Product p : products) {
            int qty = safeInt(p.getStock());
            totalQty += qty;
            ProductStock ps = stockMap.get(p.getId());
            int warning = ps != null && ps.getWarningQuantity() != null
                    ? ps.getWarningQuantity() : DEFAULT_WARNING;
            String status = resolveStockStatus(qty, warning);
            if ("OUT".equals(status)) {
                out++;
            } else if ("LOW".equals(status)) {
                low++;
            }
        }
        vo.setLowStockCount(low);
        vo.setOutOfStockCount(out);
        vo.setTotalQuantity(totalQty);
        return vo;
    }

    public PageResult<StockListVO> getStockList(
            Long supplierId,
            String keyword,
            String stockFilter,
            Integer page,
            Integer pageSize) {
        String filter = StringUtils.hasText(stockFilter) ? stockFilter.trim().toUpperCase() : null;

        // 正常/偏低依赖各商品预警阈值，需按计算状态筛选后再分页
        if ("NORMAL".equals(filter) || "LOW".equals(filter)) {
            LambdaQueryWrapper<Product> wrapper = buildProductWrapper(supplierId, keyword, null, null);
            wrapper.orderByAsc(Product::getStock);
            List<Product> candidates = productService.list(wrapper);
            Map<Long, ProductStock> stockMap = loadStockMap(candidates);

            List<Product> matched = new ArrayList<>();
            for (Product p : candidates) {
                int warning = resolveWarning(stockMap.get(p.getId()));
                String status = resolveStockStatus(safeInt(p.getStock()), warning);
                if (filter.equals(status)) {
                    matched.add(p);
                }
            }

            long total = matched.size();
            int from = Math.max(0, (page - 1) * pageSize);
            int to = Math.min(from + pageSize, matched.size());
            List<Product> pageSlice = from < to ? matched.subList(from, to) : List.of();
            return PageResult.of(total, page, pageSize, toVoList(pageSlice, stockMap));
        }

        Page<Product> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Product> wrapper = buildProductWrapper(supplierId, keyword, filter, null);
        wrapper.orderByAsc(Product::getStock);
        IPage<Product> result = productService.page(pageInfo, wrapper);
        List<Product> records = result.getRecords();
        Map<Long, ProductStock> stockMap = loadStockMap(records);
        return PageResult.of(result.getTotal(), page, pageSize, toVoList(records, stockMap));
    }

    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(
            StockAdjustRequest request,
            Long supplierId,
            Long operatorId,
            String operatorName) {
        Product product = requireProduct(request.getProductId(), supplierId);
        int before = safeInt(product.getStock());
        int change = request.getQuantity() == null ? 0 : request.getQuantity();
        int after;

        switch (request.getChangeType()) {
            case 1:
                after = before + change;
                break;
            case 2:
                if (before < change) {
                    throw new BusinessException("库存不足，当前库存 " + before);
                }
                after = before - change;
                break;
            case 3:
                after = change;
                change = after - before;
                break;
            default:
                throw new BusinessException("无效的调整类型");
        }

        if (after < 0) {
            throw new BusinessException("调整后库存不能为负数");
        }

        product.setStock(after);
        productService.updateById(product);
        syncProductStockQuantity(product.getId(), after);

        StockLog log = new StockLog();
        log.setProductId(product.getId());
        log.setProductName(product.getProductName());
        log.setChangeType(request.getChangeType());
        log.setQuantityBefore(before);
        log.setQuantityChange(change);
        log.setQuantityAfter(after);
        log.setReason(StringUtils.hasText(request.getReason()) ? request.getReason().trim() : defaultReason(request.getChangeType()));
        log.setOperatorId(operatorId);
        log.setOperatorName(StringUtils.hasText(operatorName) ? operatorName : "管理员");
        log.setSupplierId(product.getSupplierId());
        stockLogMapper.insert(log);
    }

    /**
     * 商品进库：增加库存，可选登记批次
     */
    @Transactional(rollbackFor = Exception.class)
    public void inboundStock(
            StockInboundRequest request,
            Long supplierId,
            Long operatorId,
            String operatorName) {
        Product product = requireProduct(request.getProductId(), supplierId);
        int qty = request.getQuantity();
        if (qty <= 0) {
            throw new BusinessException("进库数量必须大于 0");
        }

        if (request.getProductionDate() != null && request.getExpiryDate() != null
                && request.getExpiryDate().isBefore(request.getProductionDate())) {
            throw new BusinessException("有效期不能早于生产日期");
        }

        int before = safeInt(product.getStock());
        int after = before + qty;
        product.setStock(after);
        productService.updateById(product);
        syncProductStockQuantity(product.getId(), after);

        if (StringUtils.hasText(request.getBatchNumber())) {
            upsertBatchStock(
                    product.getId(),
                    request.getBatchNumber().trim(),
                    qty,
                    request.getProductionDate(),
                    request.getExpiryDate()
            );
        }

        StockLog log = new StockLog();
        log.setProductId(product.getId());
        log.setProductName(product.getProductName());
        log.setChangeType(1);
        log.setQuantityBefore(before);
        log.setQuantityChange(qty);
        log.setQuantityAfter(after);
        log.setReason(buildInboundReason(request));
        log.setOperatorId(operatorId);
        log.setOperatorName(StringUtils.hasText(operatorName) ? operatorName : "管理员");
        log.setSupplierId(product.getSupplierId());
        stockLogMapper.insert(log);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateWarning(StockWarningRequest request, Long supplierId) {
        Product product = requireProduct(request.getProductId(), supplierId);
        ProductStock ps = productStockService.getByProductId(product.getId());
        if (ps == null) {
            ps = new ProductStock();
            ps.setProductId(product.getId());
            ps.setQuantity(safeInt(product.getStock()));
            ps.setLockedStock(0);
        }
        ps.setWarningQuantity(request.getWarningQuantity());
        if (ps.getId() == null) {
            productStockService.save(ps);
        } else {
            productStockService.updateById(ps);
        }
    }

    public PageResult<StockLog> getStockLogs(Long productId, Long supplierId, Integer page, Integer pageSize) {
        requireProduct(productId, supplierId);
        Page<StockLog> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<StockLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockLog::getProductId, productId);
        wrapper.orderByDesc(StockLog::getCreateTime);
        IPage<StockLog> result = stockLogMapper.selectPage(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    public List<ProductBatch> getProductBatches(Long productId, Long supplierId) {
        requireProduct(productId, supplierId);
        return productBatchMapper.selectList(
                new LambdaQueryWrapper<ProductBatch>()
                        .eq(ProductBatch::getProductId, productId)
                        .orderByDesc(ProductBatch::getExpiryDate)
        );
    }

    /**
     * 订单发货出库：按订单明细扣减商品库存并记日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductForOrderShipment(Long orderId, Long operatorId, String operatorName) {
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            if (item.getProductId() == null || item.getQuantity() == null) {
                continue;
            }
            StockAdjustRequest req = new StockAdjustRequest();
            req.setProductId(item.getProductId());
            req.setChangeType(2);
            req.setQuantity(item.getQuantity());
            req.setReason("订单发货出库 #" + orderId);
            Product product = productService.getById(item.getProductId());
            adjustStock(req, product != null ? product.getSupplierId() : null, operatorId, operatorName);
        }
    }

    private Product requireProduct(Long productId, Long supplierId) {
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (supplierId != null && !supplierId.equals(product.getSupplierId())) {
            throw new BusinessException("无权操作该商品库存");
        }
        return product;
    }

    private LambdaQueryWrapper<Product> buildProductWrapper(
            Long supplierId,
            String keyword,
            String stockFilter,
            Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(Product::getSupplierId, supplierId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getBrand, keyword)
                    .or()
                    .like(Product::getManufacturer, keyword)
                    .or()
                    .like(Product::getApprovalNumber, keyword));
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        if ("OUT".equalsIgnoreCase(stockFilter)) {
            wrapper.le(Product::getStock, 0);
        }
        return wrapper;
    }

    private List<StockListVO> toVoList(List<Product> products, Map<Long, ProductStock> stockMap) {
        List<StockListVO> list = new ArrayList<>();
        for (Product p : products) {
            ProductStock ps = stockMap.get(p.getId());
            int warning = resolveWarning(ps);
            int qty = safeInt(p.getStock());
            StockListVO vo = new StockListVO();
            vo.setProductId(p.getId());
            vo.setProductName(p.getProductName());
            vo.setMainImage(p.getMainImage());
            vo.setBrand(p.getBrand());
            vo.setManufacturer(p.getManufacturer());
            vo.setSpecification(p.getSpecification());
            vo.setStock(qty);
            vo.setSales(p.getSales());
            vo.setStatus(p.getStatus());
            vo.setSupplierId(p.getSupplierId());
            vo.setWholesalePrice(p.getWholesalePrice());
            vo.setWarningQuantity(warning);
            vo.setLockedStock(ps != null && ps.getLockedStock() != null ? ps.getLockedStock() : 0);
            vo.setStockStatus(resolveStockStatus(qty, warning));
            list.add(vo);
        }
        return list;
    }

    private static int resolveWarning(ProductStock ps) {
        if (ps != null && ps.getWarningQuantity() != null) {
            return ps.getWarningQuantity();
        }
        return DEFAULT_WARNING;
    }

    private Map<Long, ProductStock> loadStockMap(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return new HashMap<>();
        }
        List<Long> ids = products.stream().map(Product::getId).collect(Collectors.toList());
        List<ProductStock> stocks = productStockService.list(
                new LambdaQueryWrapper<ProductStock>().in(ProductStock::getProductId, ids)
        );
        Map<Long, ProductStock> map = new HashMap<>();
        for (ProductStock ps : stocks) {
            map.put(ps.getProductId(), ps);
        }
        return map;
    }

    private void syncProductStockQuantity(Long productId, int quantity) {
        ProductStock ps = productStockService.getByProductId(productId);
        if (ps == null) {
            ps = new ProductStock();
            ps.setProductId(productId);
            ps.setWarningQuantity(DEFAULT_WARNING);
            ps.setLockedStock(0);
        }
        ps.setQuantity(quantity);
        if (ps.getId() == null) {
            productStockService.save(ps);
        } else {
            productStockService.updateById(ps);
        }
    }

    private static String resolveStockStatus(int qty, int warning) {
        if (qty <= 0) {
            return "OUT";
        }
        if (qty <= warning) {
            return "LOW";
        }
        return "NORMAL";
    }

    private static int safeInt(Integer v) {
        return v == null ? 0 : v;
    }

    private void upsertBatchStock(
            Long productId,
            String batchNumber,
            int quantity,
            LocalDate productionDate,
            LocalDate expiryDate) {
        ProductBatch batch = productBatchMapper.selectOne(
                new LambdaQueryWrapper<ProductBatch>()
                        .eq(ProductBatch::getProductId, productId)
                        .eq(ProductBatch::getBatchNumber, batchNumber)
                        .last("LIMIT 1")
        );
        if (batch == null) {
            batch = new ProductBatch();
            batch.setProductId(productId);
            batch.setBatchNumber(batchNumber);
            batch.setProductionDate(productionDate != null ? productionDate : LocalDate.now());
            batch.setExpiryDate(expiryDate != null ? expiryDate : LocalDate.now().plusYears(2));
            batch.setStock(quantity);
            batch.setLockedStock(0);
            productBatchMapper.insert(batch);
        } else {
            int batchStock = batch.getStock() == null ? 0 : batch.getStock();
            batch.setStock(batchStock + quantity);
            if (productionDate != null) {
                batch.setProductionDate(productionDate);
            }
            if (expiryDate != null) {
                batch.setExpiryDate(expiryDate);
            }
            productBatchMapper.updateById(batch);
        }
    }

    private static String buildInboundReason(StockInboundRequest request) {
        StringBuilder sb = new StringBuilder("进库");
        if (StringUtils.hasText(request.getBatchNumber())) {
            sb.append(" · 批号 ").append(request.getBatchNumber().trim());
        }
        if (StringUtils.hasText(request.getReason())) {
            sb.append(" · ").append(request.getReason().trim());
        }
        return sb.toString();
    }

    private static String defaultReason(Integer changeType) {
        if (changeType == null) {
            return "库存调整";
        }
        switch (changeType) {
            case 1:
                return "入库";
            case 2:
                return "出库";
            case 3:
                return "盘点";
            default:
                return "库存调整";
        }
    }
}

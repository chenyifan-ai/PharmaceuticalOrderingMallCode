package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.dto.ProductQueryRequest;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务类
 */
@Slf4j
@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    /**
     * C端: 分页查询商品列表（增强版，支持多维度筛选）
     */
    public PageResult<Product> getProductList(Long categoryId, String keyword, Integer page, Integer pageSize) {
        ProductQueryRequest request = new ProductQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setCategoryId(categoryId);
        request.setKeyword(keyword);
        return getProductListEnhanced(request);
    }

    /**
     * C端: 增强版商品列表查询（支持多维度筛选和排序）
     */
    public PageResult<Product> getProductListEnhanced(ProductQueryRequest request) {
        Page<Product> pageInfo = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查询已上架且审核通过的商品
        wrapper.eq(Product::getStatus, 1);

        // 分类筛选
        if (request.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, request.getCategoryId());
        }

        // 处方类型筛选
        if (StringUtils.hasText(request.getPrescriptionType())) {
            wrapper.eq(Product::getPrescriptionType, request.getPrescriptionType());
        }

        // 品牌筛选
        if (StringUtils.hasText(request.getBrand())) {
            wrapper.eq(Product::getBrand, request.getBrand());
        }

        // 品牌ID筛选
        if (request.getBrandId() != null) {
            wrapper.eq(Product::getBrandId, request.getBrandId());
        }

        // 剂型筛选
        if (StringUtils.hasText(request.getDosageForm())) {
            wrapper.eq(Product::getDosageForm, request.getDosageForm());
        }

        // 供应商筛选
        if (request.getSupplierId() != null) {
            wrapper.eq(Product::getSupplierId, request.getSupplierId());
        }

        // 关键词搜索（商品名、通用名、品牌、厂家、功效）
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Product::getProductName, request.getKeyword())
                    .or().like(Product::getGenericName, request.getKeyword())
                    .or().like(Product::getBrand, request.getKeyword())
                    .or().like(Product::getManufacturer, request.getKeyword())
                    .or().like(Product::getIndications, request.getKeyword()));
        }

        // 价格区间筛选
        if (request.getMinPrice() != null) {
            wrapper.ge(Product::getWholesalePrice, request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            wrapper.le(Product::getWholesalePrice, request.getMaxPrice());
        }

        // 库存筛选
        if (Boolean.TRUE.equals(request.getInStockOnly())) {
            wrapper.gt(Product::getStock, 0);
        }

        // 排序
        applySorting(wrapper, request.getSortBy());

        IPage<Product> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), request.getPage(), request.getPageSize(), result.getRecords());
    }

    /**
     * C端商品列表查询（新接口）
     */
    public PageResult<Product> getProductListC(ProductQueryRequest request) {
        return getProductListEnhanced(request);
    }

    /**
     * 应用排序规则
     */
    private void applySorting(LambdaQueryWrapper<Product> wrapper, String sortBy) {
        if (sortBy == null) {
            sortBy = "comprehensive";
        }

        switch (sortBy.toLowerCase()) {
            case "sales":
                // 销量从高到低
                wrapper.orderByDesc(Product::getSales);
                break;
            case "price_asc":
                // 价格从低到高
                wrapper.orderByAsc(Product::getWholesalePrice);
                break;
            case "price_desc":
                // 价格从高到低
                wrapper.orderByDesc(Product::getWholesalePrice);
                break;
            case "new":
                // 新品优先（按创建时间）
                wrapper.orderByDesc(Product::getCreateTime);
                break;
            case "comprehensive":
            default:
                // 综合排序：推荐 > 热销 > 创建时间
                wrapper.orderByDesc(Product::getIsRecommend)
                       .orderByDesc(Product::getIsHot)
                       .orderByDesc(Product::getSort)
                       .orderByDesc(Product::getCreateTime);
                break;
        }
    }

    /**
     * C端: 获取推荐商品
     */
    public PageResult<Product> getRecommendProducts(Integer page, Integer pageSize) {
        Page<Product> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.eq(Product::getIsRecommend, 1);
        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * C端: 热门搜索词（来自数据库热销/热门商品）
     */
    public List<String> getHotSearchKeywords() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .and(w -> w.eq(Product::getIsHot, 1).or().ge(Product::getSales, 100))
                .orderByDesc(Product::getSales)
                .last("LIMIT 8");
        return this.list(wrapper).stream()
                .map(Product::getProductName)
                .distinct()
                .toList();
    }

    /**
     * C端: 获取热门商品
     */
    public PageResult<Product> getHotProducts(Integer page, Integer pageSize) {
        Page<Product> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.eq(Product::getIsHot, 1);
        wrapper.orderByDesc(Product::getSales);

        IPage<Product> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * C端: 获取商品详情
     */
    public Product getProductDetail(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    /**
     * B端: 供应商发布商品
     */
    public void publishProduct(Product product, Long supplierId) {
        product.setSupplierId(supplierId);
        product.setStatus(2); // 待审核状态
        product.setSales(0);
        this.save(product);
    }

    /**
     * B端: 供应商更新商品
     */
    public void updateProduct(Product product, Long supplierId) {
        Product existProduct = this.getById(product.getId());
        if (existProduct == null) {
            throw new BusinessException("商品不存在");
        }

        if (!existProduct.getSupplierId().equals(supplierId)) {
            throw new BusinessException("无权操作此商品");
        }

        product.setVersion(existProduct.getVersion());
        this.updateById(product);
    }

    /**
     * B端: 供应商下架商品
     */
    public void offlineProduct(Long productId, Long supplierId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (!product.getSupplierId().equals(supplierId)) {
            throw new BusinessException("无权操作此商品");
        }

        boolean updated = this.lambdaUpdate()
                .eq(Product::getId, productId)
                .eq(Product::getSupplierId, supplierId)
                .set(Product::getStatus, 0)
                .update();
        if (!updated) {
            throw new BusinessException("下架失败");
        }
    }

    /**
     * B端: 供应商查询自己的商品列表
     */
    public PageResult<Product> getSupplierProducts(Long supplierId, Integer page, Integer pageSize, Integer status) {
        Page<Product> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSupplierId, supplierId);

        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> result = this.page(pageInfo, wrapper);

        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * 管理员: 获取商品列表
     */
    public PageResult<Product> getAdminProductList(String keyword, Integer status, Integer page, Integer pageSize) {
        Page<Product> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getProductName, keyword)
                    .or().like(Product::getGenericName, keyword)
                    .or().like(Product::getBrand, keyword)
                    .or().like(Product::getManufacturer, keyword));
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * 管理员: 审核商品
     */
    public void auditProduct(Long productId, Integer status) {
        auditProduct(productId, status, null);
    }

    public void auditProduct(Long productId, Integer status, String remark) {
        if (this.getById(productId) == null) {
            throw new BusinessException("商品不存在");
        }

        var update = this.lambdaUpdate()
                .eq(Product::getId, productId)
                .set(Product::getStatus, status);
        if (StringUtils.hasText(remark)) {
            update.set(Product::getAuditRemark, remark);
        }
        if (!update.update()) {
            throw new BusinessException("审核失败");
        }
    }

    /**
     * 管理员下架商品
     */
    public void adminOfflineProduct(Long productId) {
        if (this.getById(productId) == null) {
            throw new BusinessException("商品不存在");
        }
        boolean updated = this.lambdaUpdate()
                .eq(Product::getId, productId)
                .set(Product::getStatus, 0)
                .update();
        if (!updated) {
            throw new BusinessException("下架失败");
        }
    }

    /**
     * 管理员上架商品
     */
    public void adminOnlineProduct(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != null && product.getStatus() == 1) {
            throw new BusinessException("商品已处于上架状态");
        }
        boolean updated = this.lambdaUpdate()
                .eq(Product::getId, productId)
                .set(Product::getStatus, 1)
                .update();
        if (!updated) {
            throw new BusinessException("上架失败");
        }
    }

    /**
     * 管理员新增药品
     */
    public Product adminCreateProduct(Product product) {
        if (!StringUtils.hasText(product.getProductName())) {
            throw new BusinessException("商品名称不能为空");
        }
        if (product.getWholesalePrice() == null) {
            throw new BusinessException("批发价不能为空");
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }
        if (product.getSales() == null) {
            product.setSales(0);
        }
        if (product.getSupplierId() == null) {
            product.setSupplierId(1L);
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getPrescriptionType() == null) {
            product.setPrescriptionType("OTC");
        }
        if (product.getAuditStatus() == null) {
            product.setAuditStatus(1);
        }
        this.save(product);
        return product;
    }

    /**
     * 管理员修改药品
     */
    public void adminUpdateProduct(Product product) {
        Product exist = this.getById(product.getId());
        if (exist == null) {
            throw new BusinessException("商品不存在");
        }
        if (!StringUtils.hasText(product.getProductName())) {
            throw new BusinessException("商品名称不能为空");
        }
        product.setVersion(exist.getVersion());
        this.updateById(product);
    }

    /**
     * 管理员删除药品
     */
    public void adminDeleteProduct(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        this.removeById(productId);
    }

    /**
     * 扣减库存(使用乐观锁保证并发安全)
     * 
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    public boolean decreaseStock(Long productId, Integer quantity) {
        // 使用UPDATE语句直接扣减库存,利用数据库行锁和乐观锁保证并发安全
        int affectedRows = baseMapper.decreaseStockWithOptimisticLock(productId, quantity);
        
        if (affectedRows == 0) {
            log.warn("库存扣减失败: productId={}, quantity={}", productId, quantity);
            return false;
        }
        
        log.info("库存扣减成功: productId={}, quantity={}", productId, quantity);
        return true;
    }

    /**
     * 批量扣减库存(用于订单创建)
     * 
     * @param productIds 商品ID列表
     * @param quantities 对应的扣减数量列表
     * @return 是否全部成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDecreaseStock(List<Long> productIds, List<Integer> quantities) {
        if (productIds.size() != quantities.size()) {
            throw new BusinessException("商品ID和数量列表长度不一致");
        }

        for (int i = 0; i < productIds.size(); i++) {
            boolean success = decreaseStock(productIds.get(i), quantities.get(i));
            if (!success) {
                // 如果某个商品库存扣减失败,抛出异常触发事务回滚
                throw new BusinessException("商品库存不足或扣减失败: productId=" + productIds.get(i));
            }
        }
        
        return true;
    }

    /**
     * 增加销量
     */
    public void increaseSales(Long productId, Integer quantity) {
        Product product = this.getById(productId);
        if (product != null) {
            int sales = product.getSales() == null ? 0 : product.getSales();
            this.lambdaUpdate()
                    .eq(Product::getId, productId)
                    .set(Product::getSales, sales + quantity)
                    .update();
        }
    }

    /**
     * 设置商品的阶梯价格
     *
     * @param productId    商品ID
     * @param tierPricesJson 阶梯价格JSON字符串
     */
    public void setTierPrices(Long productId, String tierPricesJson) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证阶梯价格格式
        if (!com.yao.pharmacymall.util.TierPriceCalculator.isValid(tierPricesJson)) {
            throw new BusinessException("阶梯价格配置无效");
        }

        this.lambdaUpdate()
                .eq(Product::getId, productId)
                .set(Product::getTierPrices, tierPricesJson)
                .update();
    }

    /**
     * 获取商品的阶梯价格信息
     *
     * @param productId 商品ID
     * @return 阶梯价格JSON字符串
     */
    public String getTierPrices(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product.getTierPrices();
    }

    /**
     * 根据购买数量计算商品价格(考虑阶梯价格)
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 计算后的单价
     */
    public BigDecimal calculatePriceWithTier(Long productId, Integer quantity) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        return com.yao.pharmacymall.util.TierPriceCalculator.calculatePrice(
            product.getTierPrices(),
            quantity,
            product.getWholesalePrice()
        );
    }
}

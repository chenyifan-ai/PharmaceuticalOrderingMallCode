package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.yao.pharmacymall.dto.PackageItemDTO;
import com.yao.pharmacymall.dto.PrescriptionOrderRequest;
import com.yao.pharmacymall.enums.OrderStatus;
import com.yao.pharmacymall.enums.PrescriptionStatus;
import com.yao.pharmacymall.mapper.OrderMapper;
import com.yao.pharmacymall.mapper.ProductPackageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务类
 */
@Slf4j
@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private ProductPackageMapper productPackageMapper;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private PurchaseGuardService purchaseGuardService;

    @Autowired
    private CouponCheckoutService couponCheckoutService;

    @Autowired
    private UserCouponService userCouponService;

    private void attachInvoice(Order order) {
        if (order == null || order.getId() == null) {
            return;
        }
        order.setInvoice(invoiceService.getByOrderId(order.getId()));
    }

    private void attachInvoices(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (orderIds.isEmpty()) {
            return;
        }
        Map<Long, Invoice> invoiceMap = invoiceService.mapByOrderIds(orderIds);
        for (Order order : orders) {
            order.setInvoice(invoiceMap.get(order.getId()));
        }
    }

    /**
     * C端: 创建订单(OTC药品)
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOtcOrder(Long userId, List<Long> cartItemIds, Long addressId, Long medicationUserId, String remark,
                                Long userCouponId) {
        purchaseGuardService.requireApprovedQualification(userId);
        List<CartItem> cartItems = validateAndLoadCartItems(userId, cartItemIds);

        UserAddress address = userAddressService.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Long supplierId = null;
        for (CartItem item : cartItems) {
            Product product = productService.getById(item.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            purchaseGuardService.requireOtcProduct(product);
            purchaseGuardService.validateQuantity(product, item.getQuantity());
            long sid = item.getSeckillId() == null ? 0L : item.getSeckillId();
            if (sid > 0) {
                seckillService.requireActiveSeckill(sid, product.getId(), item.getQuantity());
            } else if (product.getStock() == null || product.getStock() < item.getQuantity()) {
                throw new BusinessException("库存不足: " + product.getProductName());
            }
            BigDecimal unitPrice = resolveCartUnitPrice(item, product, sid);
            totalAmount = totalAmount.add(unitPrice.multiply(new BigDecimal(item.getQuantity())));
            if (supplierId == null) {
                supplierId = product.getSupplierId();
            }
        }

        BigDecimal discount = couponCheckoutService.calculateDiscount(userCouponId, userId, totalAmount);
        BigDecimal payAmount = totalAmount.subtract(discount).max(BigDecimal.ZERO);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setOrderType(1);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discount);
        order.setUserCouponId(userCouponId);
        order.setFreight(BigDecimal.ZERO);
        order.setPayAmount(payAmount);
        order.setMedicationUserId(medicationUserId);
        order.setMerchantId(supplierId);
        order.setRemark(remark);

        this.save(order);

        for (CartItem item : cartItems) {
            Product product = productService.getById(item.getProductId());
            long sid = item.getSeckillId() == null ? 0L : item.getSeckillId();
            BigDecimal unitPrice = resolveCartUnitPrice(item, product, sid);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(unitPrice);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(unitPrice.multiply(new BigDecimal(item.getQuantity())));
            orderItem.setSpecification(product.getSpecification());
            orderItem.setSeckillId(sid);
            orderItemService.save(orderItem);

            productService.decreaseStock(product.getId(), item.getQuantity());
            if (sid > 0) {
                seckillService.increaseSold(sid, item.getQuantity());
            }
        }

        cartService.removeByIds(cartItemIds);

        return order;
    }

    /**
     * C端: 套餐直接下单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createPackageOrder(Long userId, Long packageId, Long addressId, Long medicationUserId, String remark) {
        purchaseGuardService.requireApprovedQualification(userId);
        ProductPackage pkg = productPackageMapper.selectById(packageId);
        if (pkg == null || pkg.getStatus() == null || pkg.getStatus() != 1) {
            throw new BusinessException("套餐不存在或已下架");
        }
        if (pkg.getStock() != null && pkg.getStock() <= 0) {
            throw new BusinessException("套餐库存不足");
        }

        List<PackageItemDTO> packageItems = parsePackageItems(pkg.getItems());
        if (packageItems.isEmpty()) {
            throw new BusinessException("套餐商品配置无效");
        }

        UserAddress address = userAddressService.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }

        BigDecimal originalTotal = BigDecimal.ZERO;
        Long supplierId = null;
        List<LineCalc> lines = new ArrayList<>();
        for (PackageItemDTO dto : packageItems) {
            Product product = productService.getById(dto.getProductId());
            if (product == null) {
                throw new BusinessException("套餐内商品不存在");
            }
            int qty = dto.getQuantity() == null || dto.getQuantity() < 1 ? 1 : dto.getQuantity();
            purchaseGuardService.validateQuantity(product, qty);
            if (product.getStock() < qty) {
                throw new BusinessException("库存不足: " + product.getProductName());
            }
            BigDecimal lineOriginal = product.getWholesalePrice().multiply(new BigDecimal(qty));
            originalTotal = originalTotal.add(lineOriginal);
            if (supplierId == null) {
                supplierId = product.getSupplierId();
            }
            lines.add(new LineCalc(product, qty, lineOriginal));
        }

        BigDecimal payAmount = pkg.getPackagePrice() != null ? pkg.getPackagePrice() : originalTotal;
        BigDecimal discount = originalTotal.subtract(payAmount);
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setOrderType(3);
        order.setPackageId(packageId);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setTotalAmount(originalTotal);
        order.setDiscountAmount(discount);
        order.setFreight(BigDecimal.ZERO);
        order.setPayAmount(payAmount);
        order.setMedicationUserId(medicationUserId);
        order.setMerchantId(supplierId);
        order.setRemark(remark != null && !remark.isBlank()
                ? remark
                : "套餐订单：" + pkg.getPackageName());
        this.save(order);

        allocatePackageLinePrices(lines, payAmount, originalTotal);
        for (LineCalc line : lines) {
            Product product = line.product;
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(line.unitPrice);
            orderItem.setQuantity(line.quantity);
            orderItem.setSubtotal(line.subtotal);
            orderItem.setSpecification(product.getSpecification());
            orderItem.setSeckillId(0L);
            orderItemService.save(orderItem);
            productService.decreaseStock(product.getId(), line.quantity);
        }

        if (pkg.getStock() != null) {
            pkg.setStock(pkg.getStock() - 1);
            productPackageMapper.updateById(pkg);
        }

        return order;
    }

    private BigDecimal resolveCartUnitPrice(CartItem item, Product product, long seckillId) {
        if (seckillId > 0) {
            SeckillItem seckill = seckillService.requireActiveSeckill(seckillId, product.getId(), item.getQuantity());
            return seckill.getSeckillPrice();
        }
        if (item.getPrice() != null) {
            return item.getPrice();
        }
        return product.getWholesalePrice();
    }

    private List<PackageItemDTO> parsePackageItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isBlank()) {
            return List.of();
        }
        List<PackageItemDTO> items = JSON.parseObject(itemsJson, new TypeReference<List<PackageItemDTO>>() {});
        return items == null ? List.of() : items;
    }

    private void allocatePackageLinePrices(List<LineCalc> lines, BigDecimal payAmount, BigDecimal originalTotal) {
        if (lines.isEmpty()) {
            return;
        }
        if (originalTotal.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal each = payAmount.divide(new BigDecimal(lines.size()), 2, RoundingMode.HALF_UP);
            for (LineCalc line : lines) {
                line.unitPrice = each.divide(new BigDecimal(line.quantity), 2, RoundingMode.HALF_UP);
                line.subtotal = line.unitPrice.multiply(new BigDecimal(line.quantity));
            }
            return;
        }
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            LineCalc line = lines.get(i);
            if (i == lines.size() - 1) {
                line.subtotal = payAmount.subtract(allocated);
            } else {
                line.subtotal = line.lineOriginal
                        .multiply(payAmount)
                        .divide(originalTotal, 2, RoundingMode.HALF_UP);
                allocated = allocated.add(line.subtotal);
            }
            line.unitPrice = line.subtotal.divide(new BigDecimal(line.quantity), 2, RoundingMode.HALF_UP);
        }
    }

    private static class LineCalc {
        private final Product product;
        private final int quantity;
        private final BigDecimal lineOriginal;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        LineCalc(Product product, int quantity, BigDecimal lineOriginal) {
            this.product = product;
            this.quantity = quantity;
            this.lineOriginal = lineOriginal;
        }
    }

    /**
     * C端: 创建处方药订单（需处方已审核通过）
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createPrescriptionOrder(Long userId, PrescriptionOrderRequest request) {
        purchaseGuardService.requireApprovedQualification(userId);
        Prescription prescription = prescriptionService.getById(request.getPrescriptionId());
        if (prescription == null || !prescription.getUserId().equals(userId)) {
            throw new BusinessException("处方不存在");
        }
        if (!PrescriptionStatus.APPROVED.getCode().equals(prescription.getAuditStatus())) {
            throw new BusinessException("处方未审核通过，无法下单");
        }

        UserAddress address = userAddressService.getById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Long supplierId = null;
        for (PrescriptionOrderRequest.OrderItemDTO itemDto : request.getItems()) {
            Product product = productService.getById(itemDto.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            if (product.getStock() < itemDto.getQuantity()) {
                throw new BusinessException("库存不足: " + product.getProductName());
            }
            BigDecimal price = calculateTierPrice(product, itemDto.getQuantity());
            totalAmount = totalAmount.add(price.multiply(new BigDecimal(itemDto.getQuantity())));
            if (supplierId == null) {
                supplierId = product.getSupplierId();
            }
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setOrderType(2);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFreight(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setMedicationUserId(request.getMedicationUserId() != null
                ? request.getMedicationUserId()
                : prescription.getMedicationUserId());
        order.setMerchantId(supplierId);
        order.setRemark(request.getRemark());
        order.setInvoiceTitle(request.getInvoiceTitle());
        order.setInvoiceTaxNo(request.getInvoiceTaxNo());
        this.save(order);

        for (PrescriptionOrderRequest.OrderItemDTO itemDto : request.getItems()) {
            Product product = productService.getById(itemDto.getProductId());
            BigDecimal price = calculateTierPrice(product, itemDto.getQuantity());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(price);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setSubtotal(price.multiply(new BigDecimal(itemDto.getQuantity())));
            orderItem.setSpecification(product.getSpecification());
            orderItemService.save(orderItem);
            productService.decreaseStock(product.getId(), itemDto.getQuantity());
        }

        prescription.setOrderId(order.getId());
        prescriptionService.updateById(prescription);

        return order;
    }

    private List<CartItem> validateAndLoadCartItems(Long userId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new BusinessException("购物车为空");
        }
        List<CartItem> cartItems = cartService.listByIds(cartItemIds);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException("购物车为空");
        }
        for (CartItem item : cartItems) {
            if (!userId.equals(item.getUserId())) {
                throw new BusinessException("购物车数据异常");
            }
        }
        return cartItems;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * C端: 获取用户订单列表
     */
    public PageResult<Order> getUserOrders(Long userId, Integer status, Integer page, Integer pageSize) {
        Page<Order> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> result = this.page(pageInfo, wrapper);
        List<Order> records = result.getRecords();
        for (Order order : records) {
            order.setOrderItems(orderItemService.getOrderItems(order.getId()));
        }
        attachInvoices(records);
        return PageResult.of(result.getTotal(), page, pageSize, records);
    }

    /**
     * C端: 获取订单详情
     */
    public Order getOrderDetail(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        order.setOrderItems(orderItemService.getOrderItems(orderId));
        attachInvoice(order);
        return order;
    }

    /**
     * C端: 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, String reason) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT.getCode())) {
            throw new BusinessException("只能取消待付款订单");
        }

        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        this.updateById(order);

        restoreStock(orderId);
        restoreSeckillSold(orderId);
        log.info("订单已取消并恢复库存: orderId={}, userId={}", orderId, userId);
    }

    private void restoreSeckillSold(Long orderId) {
        List<OrderItem> items = orderItemService.getOrderItems(orderId);
        for (OrderItem item : items) {
            Long sid = item.getSeckillId();
            if (sid != null && sid > 0 && item.getQuantity() != null) {
                seckillService.decreaseSold(sid, item.getQuantity());
            }
        }
    }

    /**
     * C端: 确认收货
     */
    public void confirmReceive(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getStatus().equals(OrderStatus.SHIPPED.getCode())) {
            throw new BusinessException("订单状态不正确");
        }

        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setReceiveTime(LocalDateTime.now());
        this.updateById(order);
    }

    /**
     * B端: 供应商获取订单列表
     */
    public PageResult<Order> getSupplierOrders(Long supplierId, Integer status, Integer page, Integer pageSize) {
        Page<Order> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, supplierId);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> result = this.page(pageInfo, wrapper);
        List<Order> records = result.getRecords();
        attachInvoices(records);
        return PageResult.of(result.getTotal(), page, pageSize, records);
    }

    /**
     * B端: 供应商发货
     */
    public void shipOrder(Long supplierId, Long orderId, String logisticsCompany, String logisticsNo) {
        Order order = this.getById(orderId);
        if (order == null || !order.getMerchantId().equals(supplierId)) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getStatus().equals(OrderStatus.PENDING_SHIPMENT.getCode())) {
            throw new BusinessException("订单状态不正确");
        }

        order.setStatus(OrderStatus.SHIPPED.getCode());
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNo(logisticsNo);
        order.setShipTime(LocalDateTime.now());
        this.updateById(order);
    }

    /**
     * 计算阶梯价格
     */
    private BigDecimal calculateTierPrice(Product product, Integer quantity) {
        return com.yao.pharmacymall.util.TierPriceCalculator.calculatePrice(
                product.getTierPrices(),
                quantity,
                product.getWholesalePrice()
        );
    }

    /**
     * 更新订单中商品的销量(支付成功后调用)
     */
    public void updateProductSales(Long orderId) {
        List<OrderItem> items = orderItemService.getOrderItems(orderId);
        for (OrderItem item : items) {
            productService.increaseSales(item.getProductId(), item.getQuantity());
        }
    }

    /**
     * 恢复订单中商品的库存(支付失败或订单取消时调用)
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreStock(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT.getCode())
                && !order.getStatus().equals(OrderStatus.CANCELLED.getCode())) {
            log.warn("订单状态不允许恢复库存: orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        List<OrderItem> items = orderItemService.getOrderItems(orderId);
        for (OrderItem item : items) {
            if (item.getProductId() == null || item.getQuantity() == null) {
                continue;
            }
            boolean restored = productService.lambdaUpdate()
                    .eq(Product::getId, item.getProductId())
                    .setSql("stock = stock + " + item.getQuantity())
                    .update();
            if (restored) {
                log.info("恢复库存: productId={}, quantity={}", item.getProductId(), item.getQuantity());
            }
        }
    }

    /**
     * 管理员: 获取订单列表
     */
    public PageResult<Order> getAdminOrderList(String keyword, Integer status, Integer page, Integer pageSize) {
        Page<Order> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (org.springframework.util.StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Order::getOrderNo, keyword)
                    .or()
                    .like(Order::getReceiverName, keyword)
                    .or()
                    .like(Order::getReceiverPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> result = this.page(pageInfo, wrapper);
        List<Order> records = result.getRecords();
        attachInvoices(records);
        return PageResult.of(result.getTotal(), page, pageSize, records);
    }

    /**
     * 管理员: 获取订单详情
     */
    public Order getAdminOrderDetail(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setOrderItems(orderItemService.getOrderItems(orderId));
        attachInvoice(order);
        return order;
    }

    /**
     * 管理员代发货
     */
    public void adminShipOrder(Long orderId, String logisticsCompany, String logisticsNo) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getStatus().equals(OrderStatus.PENDING_SHIPMENT.getCode())) {
            throw new BusinessException("订单状态不正确，仅待发货订单可发货");
        }
        order.setStatus(OrderStatus.SHIPPED.getCode());
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNo(logisticsNo);
        order.setShipTime(LocalDateTime.now());
        this.updateById(order);
    }

    /**
     * 管理员取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminCancelOrder(Long orderId, String reason) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT.getCode())
                && !order.getStatus().equals(OrderStatus.PENDING_AUDIT.getCode())
                && !order.getStatus().equals(OrderStatus.PENDING_SHIPMENT.getCode())) {
            throw new BusinessException("当前订单状态不可取消");
        }
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason != null ? reason : "管理员取消");
        this.updateById(order);
        restoreStock(orderId);
    }
}
package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.CartItem;
import com.yao.pharmacymall.entity.Product;
import com.yao.pharmacymall.entity.SeckillItem;
import com.yao.pharmacymall.mapper.CartItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车服务类
 */
@Service
public class CartService extends ServiceImpl<CartItemMapper, CartItem> {

    @Autowired
    private ProductService productService;

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private PurchaseGuardService purchaseGuardService;

    /**
     * 添加商品到购物车（可选秒杀价）
     */
    public void addToCart(Long userId, Long productId, Integer quantity, Long seckillId) {
        purchaseGuardService.requireApprovedQualification(userId);
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }
        purchaseGuardService.validateQuantity(product, quantity);
        purchaseGuardService.requireOtcProduct(product);
        long sid = normalizeSeckillId(seckillId);
        BigDecimal unitPrice;
        if (sid > 0) {
            SeckillItem seckill = seckillService.requireActiveSeckill(sid, productId, quantity);
            unitPrice = seckill.getSeckillPrice();
        } else {
            if (product.getStock() == null || product.getStock() < quantity) {
                throw new BusinessException("库存不足");
            }
            unitPrice = product.getWholesalePrice();
        }

        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId);
        wrapper.eq(CartItem::getProductId, productId);
        wrapper.eq(CartItem::getSeckillId, sid);
        CartItem existItem = this.getOne(wrapper);

        if (existItem != null) {
            int newQty = existItem.getQuantity() + quantity;
            purchaseGuardService.validateQuantity(product, newQty);
            if (sid > 0) {
                seckillService.requireActiveSeckill(sid, productId, newQty);
            } else if (product.getStock() == null || product.getStock() < newQty) {
                throw new BusinessException("库存不足");
            }
            existItem.setQuantity(newQty);
            existItem.setPrice(unitPrice);
            this.updateById(existItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setSeckillId(sid);
            cartItem.setProductName(product.getProductName());
            cartItem.setProductImage(product.getMainImage());
            cartItem.setPrice(unitPrice);
            cartItem.setQuantity(quantity);
            cartItem.setChecked(1);
            this.save(cartItem);
        }
    }

    public void addToCart(Long userId, Long productId, Integer quantity) {
        addToCart(userId, productId, quantity, 0L);
    }

    /**
     * 获取用户购物车列表
     */
    public List<CartItem> getCartList(Long userId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId);
        wrapper.orderByDesc(CartItem::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 更新购物车商品数量
     */
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = this.getById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        if (quantity <= 0) {
            throw new BusinessException("数量必须大于0");
        }

        Product product = productService.getById(cartItem.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        purchaseGuardService.validateQuantity(product, quantity);
        long sid = normalizeSeckillId(cartItem.getSeckillId());
        if (sid > 0) {
            seckillService.requireActiveSeckill(sid, cartItem.getProductId(), quantity);
        } else if (product.getStock() == null || product.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }

        cartItem.setQuantity(quantity);
        this.updateById(cartItem);
    }

    public void removeCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = this.getById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        this.removeById(cartItemId);
    }

    public void checkCartItem(Long userId, Long cartItemId, Integer checked) {
        CartItem cartItem = this.getById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        cartItem.setChecked(checked);
        this.updateById(cartItem);
    }

    public void checkAll(Long userId, Integer checked) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId);
        List<CartItem> cartItems = this.list(wrapper);
        for (CartItem cartItem : cartItems) {
            cartItem.setChecked(checked);
        }
        this.updateBatchById(cartItems);
    }

    public void clearCart(Long userId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId);
        this.remove(wrapper);
    }

    private static long normalizeSeckillId(Long seckillId) {
        return seckillId == null ? 0L : seckillId;
    }
}

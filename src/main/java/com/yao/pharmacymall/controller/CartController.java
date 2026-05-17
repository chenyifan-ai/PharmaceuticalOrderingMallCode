package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.CartItem;
import com.yao.pharmacymall.service.CartService;
import com.yao.pharmacymall.util.RequestParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<?> addToCart(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = RequestParamUtil.toLong(params.get("productId"), "商品ID");
        Integer quantity = RequestParamUtil.toInt(params.get("quantity"), 1);
        Long seckillId = RequestParamUtil.toLongOrNull(params.get("seckillId"));
        cartService.addToCart(userId, productId, quantity, seckillId == null ? 0L : seckillId);
        return Result.success("添加成功", null);
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Result<List<CartItem>> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CartItem> list = cartService.getCartList(userId);
        return Result.success(list);
    }

    /**
     * 更新购物车商品数量（前端发送 { id: xxx, quantity: xxx }）
     */
    @PutMapping("/update")
    public Result<?> updateQuantity(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = ((Number) params.get("id")).longValue();
        Integer quantity = params.get("quantity") != null
                ? ((Number) params.get("quantity")).intValue()
                : 1;
        cartService.updateQuantity(userId, id, quantity);
        return Result.success("更新成功", null);
    }

    /**
     * 删除购物车项（前端 DELETE /api/cart/{id}）
     */
    @DeleteMapping("/{id}")
    public Result<?> removeCartItem(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.removeCartItem(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<?> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearCart(userId);
        return Result.success("清空成功", null);
    }
}

package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Merchant;
import com.yao.pharmacymall.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 商家信息控制器(B端)
 */
@RestController
@RequestMapping("/api/merchant/info")
public class MerchantInfoController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 获取商家自己的信息
     */
    @GetMapping("/me")
    public Result<Merchant> getMyInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Merchant merchant = merchantService.getMerchantByUserId(userId);
        return Result.success(merchant);
    }

    /**
     * 更新商家信息
     */
    @PutMapping("/update")
    public Result<?> updateInfo(HttpServletRequest request, @RequestBody Merchant merchant) {
        Long userId = (Long) request.getAttribute("userId");
        merchantService.updateMerchantInfo(userId, merchant);
        return Result.success("更新成功，等待审核", null);
    }
}

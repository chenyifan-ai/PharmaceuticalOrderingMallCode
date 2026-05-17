package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Merchant;
import com.yao.pharmacymall.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 管理员商家管理控制器
 */
@RestController
@RequestMapping("/api/admin/merchant")
public class AdminMerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 获取商家列表
     */
    @GetMapping("/list")
    public Result<PageResult<Merchant>> getMerchantList(
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageResult<Merchant> result = merchantService.getMerchantList(auditStatus, keyword, page, pageSize);
        return Result.success(result);
    }

    /**
     * 审核商家
     */
    @PostMapping("/audit/{id}")
    public Result<?> auditMerchant(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> params) {
        Long auditorId = (Long) request.getAttribute("userId");
        Integer auditStatus = (Integer) params.get("auditStatus");
        String auditRemark = (String) params.get("auditRemark");
        merchantService.auditMerchant(auditorId, id, auditStatus, auditRemark);
        return Result.success("审核成功", null);
    }

    /**
     * 获取商家详情
     */
    @GetMapping("/{id}")
    public Result<Merchant> getMerchantDetail(@PathVariable Long id) {
        Merchant merchant = merchantService.getById(id);
        return Result.success(merchant);
    }

    @PostMapping
    public Result<Merchant> createMerchant(@RequestBody Merchant merchant) {
        Merchant created = merchantService.adminCreateMerchant(merchant);
        return Result.success("添加成功", created);
    }

    @PutMapping("/{id}")
    public Result<?> updateMerchant(@PathVariable Long id, @RequestBody Merchant merchant) {
        merchant.setId(id);
        merchantService.adminUpdateMerchant(merchant);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteMerchant(@PathVariable Long id) {
        merchantService.adminDeleteMerchant(id);
        return Result.success("删除成功", null);
    }
}

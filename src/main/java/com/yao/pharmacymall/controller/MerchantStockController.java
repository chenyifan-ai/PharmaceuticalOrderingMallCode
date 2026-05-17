package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.StockAdjustRequest;
import com.yao.pharmacymall.dto.StockInboundRequest;
import com.yao.pharmacymall.dto.StockListVO;
import com.yao.pharmacymall.dto.StockSummaryVO;
import com.yao.pharmacymall.dto.StockWarningRequest;
import com.yao.pharmacymall.entity.ProductBatch;
import com.yao.pharmacymall.entity.StockLog;
import com.yao.pharmacymall.service.StockManageService;
import com.yao.pharmacymall.util.MerchantAuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/merchant/stock", produces = "application/json;charset=UTF-8")
@Validated
public class MerchantStockController {

    @Autowired
    private StockManageService stockManageService;

    @GetMapping("/summary")
    public Result<StockSummaryVO> summary(HttpServletRequest request) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        return Result.success(stockManageService.getSummary(merchantId));
    }

    @GetMapping("/list")
    public Result<PageResult<StockListVO>> list(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stockFilter,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        return Result.success(
                stockManageService.getStockList(merchantId, keyword, stockFilter, page, pageSize)
        );
    }

    @PostMapping("/inbound")
    public Result<Boolean> inbound(
            HttpServletRequest request,
            @Valid @RequestBody StockInboundRequest body) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        stockManageService.inboundStock(
                body,
                merchantId,
                (Long) request.getAttribute("userId"),
                "供应商"
        );
        return Result.success("进库成功", true);
    }

    @PostMapping("/adjust")
    public Result<Boolean> adjust(
            HttpServletRequest request,
            @Valid @RequestBody StockAdjustRequest body) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        stockManageService.adjustStock(
                body,
                merchantId,
                (Long) request.getAttribute("userId"),
                "供应商"
        );
        return Result.success("库存调整成功", true);
    }

    @PostMapping("/warning")
    public Result<Boolean> updateWarning(
            HttpServletRequest request,
            @Valid @RequestBody StockWarningRequest body) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        stockManageService.updateWarning(body, merchantId);
        return Result.success("预警值已更新", true);
    }

    @GetMapping("/logs/{productId}")
    public Result<PageResult<StockLog>> logs(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        return Result.success(stockManageService.getStockLogs(productId, merchantId, page, pageSize));
    }

    @GetMapping("/batches/{productId}")
    public Result<List<ProductBatch>> batches(
            HttpServletRequest request,
            @PathVariable Long productId) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        return Result.success(stockManageService.getProductBatches(productId, merchantId));
    }
}

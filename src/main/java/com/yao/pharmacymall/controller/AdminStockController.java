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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/admin/stock", produces = "application/json;charset=UTF-8")
@Validated
public class AdminStockController {

    @Autowired
    private StockManageService stockManageService;

    @GetMapping("/summary")
    public Result<StockSummaryVO> summary() {
        return Result.success(stockManageService.getSummary(null));
    }

    @GetMapping("/list")
    public Result<PageResult<StockListVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stockFilter,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(stockManageService.getStockList(null, keyword, stockFilter, page, pageSize));
    }

    @PostMapping("/inbound")
    public Result<Boolean> inbound(HttpServletRequest request, @Valid @RequestBody StockInboundRequest body) {
        stockManageService.inboundStock(
                body,
                null,
                (Long) request.getAttribute("userId"),
                operatorName(request)
        );
        return Result.success("进库成功", true);
    }

    @PostMapping("/adjust")
    public Result<Boolean> adjust(HttpServletRequest request, @Valid @RequestBody StockAdjustRequest body) {
        stockManageService.adjustStock(
                body,
                null,
                (Long) request.getAttribute("userId"),
                operatorName(request)
        );
        return Result.success("库存调整成功", true);
    }

    @PostMapping("/warning")
    public Result<Boolean> updateWarning(@Valid @RequestBody StockWarningRequest body) {
        stockManageService.updateWarning(body, null);
        return Result.success("预警值已更新", true);
    }

    @GetMapping("/logs/{productId}")
    public Result<PageResult<StockLog>> logs(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(stockManageService.getStockLogs(productId, null, page, pageSize));
    }

    @GetMapping("/batches/{productId}")
    public Result<List<ProductBatch>> batches(@PathVariable Long productId) {
        return Result.success(stockManageService.getProductBatches(productId, null));
    }

    private String operatorName(HttpServletRequest request) {
        Object name = request.getAttribute("nickname");
        if (name != null && String.valueOf(name).length() > 0) {
            return String.valueOf(name);
        }
        return "平台管理员";
    }
}

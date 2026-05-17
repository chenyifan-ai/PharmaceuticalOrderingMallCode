package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Invoice;
import com.yao.pharmacymall.service.InvoiceService;
import com.yao.pharmacymall.util.MerchantAuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 商家发票控制器(B端)
 */
@RestController
@RequestMapping("/api/merchant/invoice")
public class MerchantInvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    /**
     * 获取商家发票列表
     */
    @GetMapping("/list")
    public Result<PageResult<Invoice>> getInvoiceList(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        PageResult<Invoice> result = invoiceService.getMerchantInvoices(merchantId, status, page, pageSize);
        return Result.success(result);
    }

    /**
     * 开具发票
     */
    @PostMapping("/issue/{id}")
    public Result<?> issueInvoice(HttpServletRequest request, @PathVariable Long id) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        invoiceService.issueInvoice(merchantId, id);
        return Result.success("开票成功", null);
    }

    /**
     * 寄送发票
     */
    @PostMapping("/send/{id}")
    public Result<?> sendInvoice(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, String> params) {
        Long merchantId = MerchantAuthHelper.requireMerchantId(request);
        String logisticsCompany = params.get("logisticsCompany");
        String logisticsNo = params.get("logisticsNo");
        invoiceService.sendInvoice(merchantId, id, logisticsCompany, logisticsNo);
        return Result.success("寄送成功", null);
    }
}

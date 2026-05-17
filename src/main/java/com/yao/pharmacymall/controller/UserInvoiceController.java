package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Invoice;
import com.yao.pharmacymall.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户发票控制器(C端)
 */
@RestController
@RequestMapping("/api/user/invoice")
public class UserInvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    /**
     * 申请开票
     */
    @PostMapping("/apply")
    public Result<?> applyInvoice(HttpServletRequest request, @RequestBody Invoice invoice) {
        Long userId = (Long) request.getAttribute("userId");
        invoiceService.applyInvoice(userId, invoice.getOrderId(), invoice);
        return Result.success("申请成功", null);
    }

    /**
     * 获取用户发票列表
     */
    @GetMapping("/list")
    public Result<PageResult<Invoice>> getInvoiceList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long userId = (Long) request.getAttribute("userId");
        PageResult<Invoice> result = invoiceService.getUserInvoices(userId, page, pageSize);
        return Result.success(result);
    }
}

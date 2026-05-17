package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Invoice;
import com.yao.pharmacymall.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员发票管理
 */
@RestController
@RequestMapping(value = "/api/admin/invoice", produces = "application/json;charset=UTF-8")
public class AdminInvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/list")
    public Result<PageResult<Invoice>> getInvoiceList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(invoiceService.getAdminInvoiceList(keyword, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Invoice> getInvoiceDetail(@PathVariable Long id) {
        return Result.success(invoiceService.getById(id));
    }

    @PostMapping("/issue/{id}")
    public Result<?> issueInvoice(@PathVariable Long id) {
        invoiceService.adminIssueInvoice(id);
        return Result.success("开票成功", null);
    }

    @PostMapping("/send/{id}")
    public Result<?> sendInvoice(@PathVariable Long id, @RequestBody Map<String, String> params) {
        invoiceService.adminSendInvoice(id, params.get("logisticsCompany"), params.get("logisticsNo"));
        return Result.success("寄送成功", null);
    }
}

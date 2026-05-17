package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.Invoice;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.enums.InvoiceStatus;
import com.yao.pharmacymall.mapper.InvoiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 发票服务类
 */
@Service
public class InvoiceService extends ServiceImpl<InvoiceMapper, Invoice> {

    @Autowired
    @Lazy
    private OrderService orderService;

    /**
     * B端: 商家获取发票列表
     */
    public PageResult<Invoice> getMerchantInvoices(Long merchantId, Integer status, Integer page, Integer pageSize) {
        Page<Invoice> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getMerchantId, merchantId);

        if (status != null) {
            wrapper.eq(Invoice::getStatus, status);
        }

        wrapper.orderByDesc(Invoice::getCreateTime);

        IPage<Invoice> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * B端: 商家开具发票
     */
    public void issueInvoice(Long merchantId, Long invoiceId) {
        Invoice invoice = this.getById(invoiceId);
        if (invoice == null || !invoice.getMerchantId().equals(merchantId)) {
            throw new BusinessException("发票不存在");
        }

        if (!invoice.getStatus().equals(InvoiceStatus.PENDING.getCode())) {
            throw new BusinessException("发票状态不正确");
        }

        // 生成发票号
        String invoiceNo = "INV" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        invoice.setInvoiceNo(invoiceNo);
        invoice.setStatus(InvoiceStatus.ISSUED.getCode());
        invoice.setInvoiceTime(LocalDateTime.now());
        this.updateById(invoice);

        // 更新订单发票状态
        Order order = orderService.getById(invoice.getOrderId());
        if (order != null) {
            order.setInvoiceStatus(1);
            orderService.updateById(order);
        }
    }

    /**
     * B端: 商家寄送发票
     */
    public void sendInvoice(Long merchantId, Long invoiceId, String logisticsCompany, String logisticsNo) {
        Invoice invoice = this.getById(invoiceId);
        if (invoice == null || !invoice.getMerchantId().equals(merchantId)) {
            throw new BusinessException("发票不存在");
        }

        if (!invoice.getStatus().equals(InvoiceStatus.ISSUED.getCode())) {
            throw new BusinessException("发票状态不正确，请先开具发票");
        }

        invoice.setLogisticsCompany(logisticsCompany);
        invoice.setLogisticsNo(logisticsNo);
        invoice.setStatus(InvoiceStatus.SENT.getCode());
        invoice.setSendTime(LocalDateTime.now());
        this.updateById(invoice);
    }

    /**
     * C端: 用户申请开票
     */
    public void applyInvoice(Long userId, Long orderId, Invoice invoice) {
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        if (order.getInvoiceStatus() != null && order.getInvoiceStatus() == 1) {
            throw new BusinessException("该订单已开具发票");
        }

        invoice.setOrderId(orderId);
        invoice.setOrderNo(order.getOrderNo());
        invoice.setUserId(userId);
        invoice.setMerchantId(order.getMerchantId());
        invoice.setAmount(order.getPayAmount());
        invoice.setStatus(InvoiceStatus.PENDING.getCode());
        this.save(invoice);
    }

    /**
     * 管理员: 发票列表
     */
    public PageResult<Invoice> getAdminInvoiceList(String keyword, Integer status, Integer page, Integer pageSize) {
        Page<Invoice> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Invoice::getOrderNo, keyword)
                    .or()
                    .like(Invoice::getInvoiceTitle, keyword)
                    .or()
                    .like(Invoice::getInvoiceNo, keyword));
        }
        if (status != null) {
            wrapper.eq(Invoice::getStatus, status);
        }
        wrapper.orderByDesc(Invoice::getCreateTime);
        IPage<Invoice> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    public void adminIssueInvoice(Long invoiceId) {
        Invoice invoice = this.getById(invoiceId);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        if (!invoice.getStatus().equals(InvoiceStatus.PENDING.getCode())) {
            throw new BusinessException("发票状态不正确");
        }
        String invoiceNo = invoice.getInvoiceNo();
        if (!StringUtils.hasText(invoiceNo)) {
            invoiceNo = "INV" + System.currentTimeMillis();
        }
        invoice.setInvoiceNo(invoiceNo);
        invoice.setStatus(InvoiceStatus.ISSUED.getCode());
        invoice.setInvoiceTime(LocalDateTime.now());
        this.updateById(invoice);
        Order order = orderService.getById(invoice.getOrderId());
        if (order != null) {
            order.setInvoiceStatus(1);
            orderService.updateById(order);
        }
    }

    public void adminSendInvoice(Long invoiceId, String logisticsCompany, String logisticsNo) {
        Invoice invoice = this.getById(invoiceId);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        if (!invoice.getStatus().equals(InvoiceStatus.ISSUED.getCode())) {
            throw new BusinessException("发票状态不正确，请先开具发票");
        }
        invoice.setLogisticsCompany(logisticsCompany);
        invoice.setLogisticsNo(logisticsNo);
        invoice.setStatus(InvoiceStatus.SENT.getCode());
        invoice.setSendTime(LocalDateTime.now());
        this.updateById(invoice);
    }

    /**
     * C端: 用户获取发票列表
     */
    /**
     * 按订单ID查询发票（一单最多一条有效记录）
     */
    public Invoice getByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return this.getOne(
                new LambdaQueryWrapper<Invoice>()
                        .eq(Invoice::getOrderId, orderId)
                        .orderByDesc(Invoice::getCreateTime)
                        .last("LIMIT 1")
        );
    }

    /**
     * 批量按订单ID查询发票
     */
    public java.util.Map<Long, Invoice> mapByOrderIds(java.util.Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<Invoice> list = this.list(
                new LambdaQueryWrapper<Invoice>()
                        .in(Invoice::getOrderId, orderIds)
                        .orderByDesc(Invoice::getCreateTime)
        );
        java.util.Map<Long, Invoice> map = new java.util.LinkedHashMap<>();
        for (Invoice inv : list) {
            map.putIfAbsent(inv.getOrderId(), inv);
        }
        return map;
    }

    public PageResult<Invoice> getUserInvoices(Long userId, Integer page, Integer pageSize) {
        Page<Invoice> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getUserId, userId);
        wrapper.orderByDesc(Invoice::getCreateTime);

        IPage<Invoice> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }
}

package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.dto.MerchantSettlementVO;
import com.yao.pharmacymall.entity.Merchant;
import com.yao.pharmacymall.entity.MerchantSettlement;
import com.yao.pharmacymall.entity.Order;
import com.yao.pharmacymall.enums.OrderStatus;
import com.yao.pharmacymall.mapper.MerchantSettlementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantSettlementService {

    private static final BigDecimal PLATFORM_RATE = new BigDecimal("0.02");

    private final MerchantSettlementMapper settlementMapper;
    private final OrderService orderService;
    private final OperationLogService operationLogService;
    private final MerchantService merchantService;

    public PageResult<MerchantSettlement> list(Long merchantId, Integer status, Integer page, Integer pageSize) {
        Page<MerchantSettlement> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<MerchantSettlement> wrapper = new LambdaQueryWrapper<>();
        if (merchantId != null) {
            wrapper.eq(MerchantSettlement::getMerchantId, merchantId);
        }
        if (status != null) {
            wrapper.eq(MerchantSettlement::getStatus, status);
        }
        wrapper.orderByDesc(MerchantSettlement::getCreateTime);
        IPage<MerchantSettlement> result = settlementMapper.selectPage(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    public PageResult<MerchantSettlementVO> listVo(Long merchantId, Integer status, Integer page, Integer pageSize) {
        PageResult<MerchantSettlement> raw = list(merchantId, status, page, pageSize);
        List<MerchantSettlementVO> vos = raw.getList().stream().map(s -> {
            MerchantSettlementVO vo = new MerchantSettlementVO();
            vo.setId(s.getId());
            vo.setMerchantId(s.getMerchantId());
            vo.setSettlementNo(s.getSettlementNo());
            vo.setPeriodStart(s.getPeriodStart());
            vo.setPeriodEnd(s.getPeriodEnd());
            vo.setOrderCount(s.getOrderCount());
            vo.setTotalAmount(s.getTotalAmount());
            vo.setPlatformFee(s.getPlatformFee());
            vo.setSettleAmount(s.getSettleAmount());
            vo.setStatus(s.getStatus());
            vo.setRemark(s.getRemark());
            vo.setSettleTime(s.getSettleTime());
            vo.setCreateTime(s.getCreateTime());
            vo.setUpdateTime(s.getUpdateTime());
            if (s.getMerchantId() != null) {
                Merchant m = merchantService.getById(s.getMerchantId());
                if (m != null) {
                    vo.setMerchantName(m.getMerchantName());
                }
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        return PageResult.of(raw.getTotal(), page, pageSize, vos);
    }

    @Transactional(rollbackFor = Exception.class)
    public MerchantSettlement generate(Long merchantId, LocalDate periodStart, LocalDate periodEnd, Long operatorId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId);
        wrapper.in(Order::getStatus,
                OrderStatus.PENDING_SHIPMENT.getCode(),
                OrderStatus.SHIPPED.getCode(),
                OrderStatus.COMPLETED.getCode());
        if (periodStart != null) {
            wrapper.ge(Order::getPayTime, periodStart.atStartOfDay());
        }
        if (periodEnd != null) {
            wrapper.le(Order::getPayTime, periodEnd.plusDays(1).atStartOfDay());
        }
        List<Order> orders = orderService.list(wrapper);

        BigDecimal total = orders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fee = total.multiply(PLATFORM_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal settle = total.subtract(fee);

        MerchantSettlement s = new MerchantSettlement();
        s.setMerchantId(merchantId);
        s.setSettlementNo("ST" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        s.setPeriodStart(periodStart);
        s.setPeriodEnd(periodEnd);
        s.setOrderCount(orders.size());
        s.setTotalAmount(total);
        s.setPlatformFee(fee);
        s.setSettleAmount(settle);
        s.setStatus(0);
        settlementMapper.insert(s);

        operationLogService.log(operatorId, "管理员", 3, "结算", "生成结算单", s.getId(),
                "merchantId=" + merchantId + ", amount=" + settle);
        return s;
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmSettle(Long id, Long operatorId) {
        MerchantSettlement s = settlementMapper.selectById(id);
        if (s == null) {
            throw new BusinessException("结算单不存在");
        }
        if (s.getStatus() != null && s.getStatus() == 1) {
            return;
        }
        s.setStatus(1);
        s.setSettleTime(LocalDateTime.now());
        settlementMapper.updateById(s);
        operationLogService.log(operatorId, "管理员", 3, "结算", "确认结算", id, null);
    }
}

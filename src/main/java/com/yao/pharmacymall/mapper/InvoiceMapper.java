package com.yao.pharmacymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.pharmacymall.entity.Invoice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发票Mapper接口
 */
@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {
}

package com.yao.pharmacymall.common;

import lombok.Data;

import java.util.List;

/**
 * 分页响应结果类
 */
@Data
public class PageResult<T> {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 数据列表
     */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(Long total, Integer page, Integer pageSize, List<T> list) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.list = list;
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(Long total, Integer page, Integer pageSize, List<T> list) {
        return new PageResult<>(total, page, pageSize, list);
    }
}

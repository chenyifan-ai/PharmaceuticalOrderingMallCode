package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.Category;
import com.yao.pharmacymall.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务类
 */
@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    /**
     * 根据父ID获取子分类列表
     */
    public List<Category> getListByParentId(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1);
        if (parentId == null) {
            wrapper.eq(Category::getParentId, 0L);
        } else {
            wrapper.eq(Category::getParentId, parentId);
        }
        wrapper.orderByAsc(Category::getSort, Category::getId);
        return this.list(wrapper);
    }

    /**
     * 获取分类树形结构
     */
    public List<Category> getCategoryTree() {
        // 查询所有启用的分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1);
        wrapper.orderByAsc(Category::getSort, Category::getId);

        List<Category> allCategories = this.list(wrapper);

        return buildCategoryTree(allCategories, 0L);
    }

    /**
     * 递归构建分类树
     */
    private List<Category> buildCategoryTree(List<Category> allCategories, Long parentId) {
        return allCategories.stream()
                .filter(category -> category.getParentId() != null && category.getParentId().equals(parentId))
                .peek(category -> {
                    // 递归设置子分类
                    List<Category> children = buildCategoryTree(allCategories, category.getId());
                    category.setChildren(children);
                })
                .collect(Collectors.toList());
    }
}

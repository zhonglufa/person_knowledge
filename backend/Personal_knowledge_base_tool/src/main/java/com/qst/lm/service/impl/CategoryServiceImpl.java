package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qst.lm.common.R;
import com.qst.lm.dto.category.CategoryDTO;
import com.qst.lm.dto.category.CategoryMoveDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.CategoryMapper;
import com.qst.lm.mapper.CollectionItemMapper;
import com.qst.lm.pojo.Category;
import com.qst.lm.service.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 分类管理服务实现类
 */
@Slf4j
@Service
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryMapper categoryMapper;
    private final CollectionItemMapper collectionItemMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, CollectionItemMapper collectionItemMapper) {
        this.categoryMapper = categoryMapper;
        this.collectionItemMapper = collectionItemMapper;
    }

    @Override
    public R createCategory(Long userId, CategoryDTO dto) {
        // 校验同一父分类下名称不重复
        Long parentId = normalizeParentId(dto.getParentId());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .eq(Category::getName, dto.getName())
                .eq(Category::getParentId, parentId);
        if (parentId == null || parentId == 0L) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getUserId, userId)
                    .eq(Category::getName, dto.getName())
                    .and(w -> w.isNull(Category::getParentId).or().eq(Category::getParentId, 0L));
        }
        Long count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("同一父分类下已存在相同名称的分类");
        }

        // 如果指定了父分类，校验父分类存在且属于当前用户
        if (parentId != null && parentId > 0) {
            Category parentCategory = categoryMapper.selectById(parentId);
            if (parentCategory == null) {
                log.error("父分类[{}]不存在，用户[{}]创建分类失败", parentId, userId);
                throw new BusinessException("父分类不存在，ID: " + parentId);
            }
            if (parentCategory.getDeleted() == 1) {
                log.error("父分类[{}]已被删除，用户[{}]创建分类失败", parentId, userId);
                throw new BusinessException("父分类已被删除，ID: " + parentId);
            }
            if (!parentCategory.getUserId().equals(userId)) {
                log.error("父分类[{}]不属于用户[{}]，创建分类失败", parentId, userId);
                throw new BusinessException("父分类不属于当前用户");
            }
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setParentId(parentId);
        category.setUserId(userId);
        category.setIcon(dto.getIcon());
        category.setColor(dto.getColor());
        categoryMapper.insert(category);

        log.info("用户[{}]创建分类[{}]成功", userId, category.getId());
        return R.success("创建分类成功", category);
    }

    @Override
    public R updateCategory(Long userId, Long id, CategoryDTO dto) {
        Category category = categoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new BusinessException("分类不存在");
        }

        Long parentId = normalizeParentId(dto.getParentId());

        // 校验同一父分类下名称不重复（排除自身）
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .eq(Category::getName, dto.getName())
                .ne(Category::getId, id);
        if (parentId == null || parentId == 0L) {
            wrapper.and(w -> w.isNull(Category::getParentId).or().eq(Category::getParentId, 0L));
        } else {
            wrapper.eq(Category::getParentId, parentId);
        }
        Long count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("同一父分类下已存在相同名称的分类");
        }

        category.setName(dto.getName());
        category.setParentId(parentId);
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        category.setColor(dto.getColor());
        categoryMapper.updateById(category);

        log.info("用户[{}]更新分类[{}]成功", userId, id);
        return R.success("更新分类成功", category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteCategory(Long userId, Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new BusinessException("分类不存在");
        }

        deleteChildrenRecursive(userId, id);

        collectionItemMapper.clearCategoryReference(id);
        categoryMapper.deleteById(id);

        log.info("用户[{}]删除分类[{}]成功（含子分类）", userId, id);
        return R.success("删除分类成功");
    }

    @Override
    public R getCategoryTree(Long userId) {
        // 查询用户所有分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .orderByAsc(Category::getId);
        List<Category> allCategories = categoryMapper.selectList(wrapper);

        // 在内存中构建树形结构
        List<Map<String, Object>> tree = buildTree(allCategories, null);
        return R.success(tree);
    }

    @Override
    public R getCategoryList(Long userId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .orderByAsc(Category::getId);
        List<Category> categories = categoryMapper.selectList(wrapper);
        return R.success(categories);
    }

    /**
     * 递归删除子分类
     */
    private void deleteChildrenRecursive(Long userId, Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .eq(Category::getParentId, parentId);
        List<Category> children = categoryMapper.selectList(wrapper);
        for (Category child : children) {
            deleteChildrenRecursive(userId, child.getId());
            collectionItemMapper.clearCategoryReference(child.getId());
            categoryMapper.deleteById(child.getId());
        }
    }

    /**
     * 构建树形结构
     */
    private List<Map<String, Object>> buildTree(List<Category> allCategories, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Category category : allCategories) {
            Long catParentId = category.getParentId();
            boolean isRoot = (parentId == null && (catParentId == null || catParentId == 0L))
                    || (parentId != null && parentId.equals(catParentId));
            if (isRoot) {
                Map<String, Object> node = new HashMap<>(16);
                node.put("id", category.getId());
                node.put("name", category.getName());
                node.put("icon", category.getIcon());
                node.put("color", category.getColor());
                node.put("parentId", category.getParentId());
                node.put("createdAt", category.getCreatedAt());
                node.put("updatedAt", category.getUpdatedAt());

                int contentCount = collectionItemMapper.countByCategoryId(category.getId());
                node.put("contentCount", contentCount);

                node.put("children", buildTree(allCategories, category.getId()));
                tree.add(node);
            }
        }
        return tree;
    }

    /**
     * 标准化父分类ID，0视为null
     */
    private Long normalizeParentId(Long parentId) {
        if (parentId != null && parentId == 0L) {
            return null;
        }
        return parentId;
    }

    @Override
    public R getStatistics(Long userId) {
        // 查询用户所有分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId);
        List<Category> categories = categoryMapper.selectList(wrapper);

        Map<String, Object> statistics = new HashMap<>(8);
        statistics.put("totalCategory", categories.size());

        // 统计每个分类下的收藏项数量
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        for (Category category : categories) {
            Map<String, Object> stat = new HashMap<>(4);
            stat.put("categoryId", category.getId());
            stat.put("categoryName", category.getName());
            int itemCount = collectionItemMapper.countByCategoryId(category.getId());
            stat.put("itemCount", itemCount);
            categoryStats.add(stat);
        }
        statistics.put("categoryStats", categoryStats);

        return R.success(statistics);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R moveCategory(Long userId, Long id, CategoryMoveDTO dto) {
        if (dto == null || dto.getTargetParentId() == null) {
            throw new BusinessException("目标父分类ID不能为空");
        }

        // 校验分类归属
        Category category = categoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new BusinessException("分类不存在");
        }

        // 不能移动到自己下面
        if (id.equals(dto.getTargetParentId())) {
            throw new BusinessException("不能将分类移动到自己下面");
        }

        // 校验目标父分类是否存在且属于当前用户(除非是移动到根分类)
        Long targetParentId = dto.getTargetParentId();
        if (targetParentId > 0) {
            Category parentCategory = categoryMapper.selectById(targetParentId);
            if (parentCategory == null || parentCategory.getDeleted() == 1 || !parentCategory.getUserId().equals(userId)) {
                throw new BusinessException("目标父分类不存在或已被删除");
            }
        }

        // 更新分类的父分类ID
        LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
        Long newParentId = targetParentId > 0 ? targetParentId : null;
        updateWrapper.eq(Category::getId, id)
                .set(Category::getParentId, newParentId);
        categoryMapper.update(null, updateWrapper);

        log.info("用户[{}]移动分类[{}]到父分类[{}]", userId, id, newParentId);
        return R.success("移动分类成功");
    }
}

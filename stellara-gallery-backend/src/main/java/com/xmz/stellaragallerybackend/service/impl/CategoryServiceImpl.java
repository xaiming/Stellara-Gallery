package com.xmz.stellaragallerybackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.mapper.CategoryMapper;
import com.xmz.stellaragallerybackend.model.dto.category.CategoryRequest;
import com.xmz.stellaragallerybackend.model.entity.Category;
import com.xmz.stellaragallerybackend.model.vo.CategoryVO;
import com.xmz.stellaragallerybackend.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * 分类业务服务实现。
 */
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /**
     * 新增分类。
     */
    @Override
    public Long addCategory(CategoryRequest categoryRequest) {
        validCategory(categoryRequest, true);
        boolean exists = this.lambdaQuery().eq(Category::getName, categoryRequest.getName().trim()).exists();
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "分类名称已存在");
        Category category = BeanUtil.copyProperties(categoryRequest, Category.class);
        category.setName(category.getName().trim());
        category.setSort(category.getSort() == null ? 0 : category.getSort());
        category.setStatus(category.getStatus() == null ? 0 : category.getStatus());
        boolean saved = this.save(category);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR);
        return category.getId();
    }

    /**
     * 更新分类。
     */
    @Override
    public Boolean updateCategory(CategoryRequest categoryRequest) {
        validCategory(categoryRequest, false);
        Category category = BeanUtil.copyProperties(categoryRequest, Category.class);
        if (StrUtil.isNotBlank(category.getName())) {
            category.setName(category.getName().trim());
        }
        boolean updated = this.updateById(category);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 查询分类列表。
     */
    @Override
    public List<CategoryVO> listCategory(Boolean enabledOnly) {
        return this.lambdaQuery()
                .eq(Boolean.TRUE.equals(enabledOnly), Category::getStatus, 0)
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime)
                .list()
                .stream()
                .map(this::getCategoryVO)
                .toList();
    }

    /**
     * 转换为分类视图对象。
     */
    @Override
    public CategoryVO getCategoryVO(Category category) {
        return category == null ? null : BeanUtil.copyProperties(category, CategoryVO.class);
    }

    /**
     * 校验分类字段。
     */
    private void validCategory(CategoryRequest categoryRequest, boolean add) {
        ThrowUtils.throwIf(categoryRequest == null, ErrorCode.PARAMS_ERROR);
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(categoryRequest.getName()), ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        } else {
            ThrowUtils.throwIf(categoryRequest.getId() == null || categoryRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isNotBlank(categoryRequest.getName())) {
            ThrowUtils.throwIf(categoryRequest.getName().trim().length() > 64, ErrorCode.PARAMS_ERROR, "分类名称过长");
        }
        if (categoryRequest.getStatus() != null) {
            ThrowUtils.throwIf(categoryRequest.getStatus() != 0 && categoryRequest.getStatus() != 1,
                    ErrorCode.PARAMS_ERROR, "分类状态不合法");
        }
    }
}

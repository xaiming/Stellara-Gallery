package com.xmz.stellaragallerybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmz.stellaragallerybackend.model.dto.category.CategoryRequest;
import com.xmz.stellaragallerybackend.model.entity.Category;
import com.xmz.stellaragallerybackend.model.vo.CategoryVO;

import java.util.List;

/**
 * 分类业务服务。
 */
public interface CategoryService extends IService<Category> {

    /**
     * 新增分类。
     */
    Long addCategory(CategoryRequest categoryRequest);

    /**
     * 更新分类。
     */
    Boolean updateCategory(CategoryRequest categoryRequest);

    /**
     * 查询全部分类。
     */
    List<CategoryVO> listCategory(Boolean enabledOnly);

    /**
     * 转换为分类视图对象。
     */
    CategoryVO getCategoryVO(Category category);
}

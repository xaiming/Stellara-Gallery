package com.xmz.stellaragallerybackend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xmz.stellaragallerybackend.common.BaseResponse;
import com.xmz.stellaragallerybackend.common.DeleteRequest;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.common.ResultUtils;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.model.dto.category.CategoryRequest;
import com.xmz.stellaragallerybackend.model.vo.CategoryVO;
import com.xmz.stellaragallerybackend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/category")
/**
 * 图片分类接口控制器。
 */
public class CategoryController {

    /**
     * 分类业务服务。
     */
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 普通用户可读取启用分类，用于图库筛选和上传表单。
     */
    @Operation(summary = "查询启用分类")
    @GetMapping("/list/enabled")
    public BaseResponse<List<CategoryVO>> listEnabledCategory() {
        return ResultUtils.success(categoryService.listCategory(true));
    }

    /**
     * 管理员查询全部分类。
     */
    @Operation(summary = "查询全部分类")
    @GetMapping("/list")
    public BaseResponse<List<CategoryVO>> listCategory() {
        StpUtil.checkRole("admin");
        return ResultUtils.success(categoryService.listCategory(false));
    }

    /**
     * 管理员新增分类。
     */
    @Operation(summary = "新增分类")
    @PostMapping("/add")
    public BaseResponse<Long> addCategory(@RequestBody CategoryRequest categoryRequest) {
        StpUtil.checkRole("admin");
        return ResultUtils.success(categoryService.addCategory(categoryRequest));
    }

    /**
     * 管理员更新分类。
     */
    @Operation(summary = "更新分类")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateCategory(@RequestBody CategoryRequest categoryRequest) {
        StpUtil.checkRole("admin");
        return ResultUtils.success(categoryService.updateCategory(categoryRequest));
    }

    /**
     * 管理员删除分类。
     */
    @Operation(summary = "删除分类")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteCategory(@RequestBody DeleteRequest deleteRequest) {
        StpUtil.checkRole("admin");
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(categoryService.removeById(deleteRequest.getId()));
    }
}

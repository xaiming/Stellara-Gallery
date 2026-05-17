package com.xmz.stellaragallerybackend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xmz.stellaragallerybackend.common.BaseResponse;
import com.xmz.stellaragallerybackend.common.DeleteRequest;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.common.ResultUtils;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.model.dto.tag.TagRequest;
import com.xmz.stellaragallerybackend.model.vo.TagVO;
import com.xmz.stellaragallerybackend.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "标签管理")
@RestController
@RequestMapping("/tag")
/**
 * 图片标签接口控制器。
 */
public class TagController {

    /**
     * 标签业务服务。
     */
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 查询标签列表，普通用户用于筛选和上传表单。
     */
    @Operation(summary = "查询启用标签")
    @GetMapping("/list/enabled")
    public BaseResponse<List<TagVO>> listEnabledTag() {
        return ResultUtils.success(tagService.listTag());
    }

    /**
     * 管理员查询全部标签。
     */
    @Operation(summary = "查询全部标签")
    @GetMapping("/list")
    public BaseResponse<List<TagVO>> listTag() {
        StpUtil.checkRole("admin");
        return ResultUtils.success(tagService.listTag());
    }

    /**
     * 管理员新增标签。
     */
    @Operation(summary = "新增标签")
    @PostMapping("/add")
    public BaseResponse<Long> addTag(@RequestBody TagRequest tagRequest) {
        StpUtil.checkRole("admin");
        return ResultUtils.success(tagService.addTag(tagRequest));
    }

    /**
     * 管理员更新标签。
     */
    @Operation(summary = "更新标签")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTag(@RequestBody TagRequest tagRequest) {
        StpUtil.checkRole("admin");
        return ResultUtils.success(tagService.updateTag(tagRequest));
    }

    /**
     * 管理员删除标签。
     */
    @Operation(summary = "删除标签")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTag(@RequestBody DeleteRequest deleteRequest) {
        StpUtil.checkRole("admin");
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(tagService.removeById(deleteRequest.getId()));
    }
}

package com.xmz.stellaragallerybackend.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmz.stellaragallerybackend.common.BaseResponse;
import com.xmz.stellaragallerybackend.common.DeleteRequest;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.common.ResultUtils;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureBatchReviewRequest;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureQueryRequest;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureReviewRequest;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureUpdateRequest;
import com.xmz.stellaragallerybackend.model.vo.PictureVO;
import com.xmz.stellaragallerybackend.service.PictureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Tag(name = "图片管理")
@RestController
@RequestMapping("/picture")
/**
 * 图片接口控制器。
 */
public class PictureController {

    /**
     * 图片业务服务。
     */
    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    /**
     * 上传图片到当前登录用户个人空间。
     */
    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile file,
                                                 @RequestParam String name,
                                                 @RequestParam(required = false) String introduction,
                                                 @RequestParam(required = false) Long categoryId,
                                                 @RequestParam(required = false) String tags,
                                                 @RequestParam(required = false, defaultValue = "0") Integer isPublic) {
        return ResultUtils.success(pictureService.uploadPicture(file, name, introduction, categoryId, parseTags(tags), isPublic));
    }

    /**
     * 获取图片详情，公共图片允许游客访问。
     */
    @Operation(summary = "获取图片详情")
    @GetMapping("/get")
    public BaseResponse<PictureVO> getPictureById(@RequestParam Long id, HttpServletRequest request) {
        return ResultUtils.success(pictureService.getPictureVOById(id, buildVisitorKey(request)));
    }

    /**
     * 分页查询公共图库。
     */
    @Operation(summary = "分页查询公共图库")
    @PostMapping("/list/public/page")
    public BaseResponse<Page<PictureVO>> listPublicPictureByPage(@RequestBody(required = false) PictureQueryRequest pictureQueryRequest) {
        return ResultUtils.success(pictureService.listPublicPictureByPage(pictureQueryRequest));
    }

    /**
     * 分页查询我的图片。
     */
    @Operation(summary = "分页查询我的图片")
    @PostMapping("/list/my/page")
    public BaseResponse<Page<PictureVO>> listMyPictureByPage(@RequestBody(required = false) PictureQueryRequest pictureQueryRequest) {
        return ResultUtils.success(pictureService.listMyPictureByPage(pictureQueryRequest));
    }

    /**
     * 管理员分页查询全部图片。
     */
    @Operation(summary = "管理员分页查询图片")
    @PostMapping("/list/admin/page")
    public BaseResponse<Page<PictureVO>> listAdminPictureByPage(@RequestBody(required = false) PictureQueryRequest pictureQueryRequest) {
        StpUtil.checkRole("admin");
        return ResultUtils.success(pictureService.listAdminPictureByPage(pictureQueryRequest));
    }

    /**
     * 分页查询当前登录用户收藏夹。
     */
    @Operation(summary = "分页查询我的收藏图片")
    @PostMapping("/favorite/list/page")
    public BaseResponse<Page<PictureVO>> listMyFavoritePictureByPage(@RequestBody(required = false) PictureQueryRequest pictureQueryRequest) {
        return ResultUtils.success(pictureService.listMyFavoritePictureByPage(pictureQueryRequest));
    }

    /**
     * 编辑图片。
     */
    @Operation(summary = "更新图片")
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
        return ResultUtils.success(pictureService.updatePicture(pictureUpdateRequest));
    }

    /**
     * 删除图片。
     */
    @Operation(summary = "删除图片")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.deletePicture(deleteRequest.getId()));
    }

    /**
     * 管理员审核图片。
     */
    @Operation(summary = "审核图片")
    @PostMapping("/review")
    public BaseResponse<Boolean> reviewPicture(@RequestBody PictureReviewRequest pictureReviewRequest) {
        StpUtil.checkRole("admin");
        return ResultUtils.success(pictureService.reviewPicture(pictureReviewRequest));
    }

    /**
     * 管理员批量审核图片。
     */
    @Operation(summary = "批量审核图片")
    @PostMapping("/review/batch")
    public BaseResponse<Boolean> batchReviewPicture(@RequestBody PictureBatchReviewRequest pictureBatchReviewRequest) {
        StpUtil.checkRole("admin");
        return ResultUtils.success(pictureService.batchReviewPicture(pictureBatchReviewRequest));
    }

    /**
     * 点赞或取消点赞。
     */
    @Operation(summary = "切换图片点赞")
    @PostMapping("/like")
    public BaseResponse<Boolean> toggleLike(@RequestBody DeleteRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.toggleLike(request.getId()));
    }

    /**
     * 收藏或取消收藏。
     */
    @Operation(summary = "切换图片收藏")
    @PostMapping("/favorite")
    public BaseResponse<Boolean> toggleFavorite(@RequestBody DeleteRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.toggleFavorite(request.getId()));
    }

    /**
     * 获取图片下载地址并累加下载次数。
     */
    @Operation(summary = "下载图片")
    @GetMapping("/download")
    public BaseResponse<String> downloadPicture(@RequestParam Long id) {
        return ResultUtils.success(pictureService.downloadPicture(id));
    }

    /**
     * 解析前端表单中的标签字段。
     */
    private List<String> parseTags(String tags) {
        if (StrUtil.isBlank(tags)) {
            return Collections.emptyList();
        }
        String trimmedTags = tags.trim();
        if (trimmedTags.startsWith("[")) {
            return JSONUtil.toList(trimmedTags, String.class);
        }
        return Arrays.stream(trimmedTags.split(","))
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .toList();
    }

    /**
     * 生成 HyperLogLog 访问者标识。
     */
    private String buildVisitorKey(HttpServletRequest request) {
        if (StpUtil.isLogin()) {
            return "user:" + StpUtil.getLoginIdAsLong();
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ip = StrUtil.isBlank(forwardedFor) ? request.getRemoteAddr() : forwardedFor.split(",")[0].trim();
        String userAgent = StrUtil.blankToDefault(request.getHeader("User-Agent"), "unknown");
        return "guest:" + DigestUtil.md5Hex(ip + "|" + userAgent);
    }
}

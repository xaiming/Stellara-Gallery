package com.xmz.stellaragallerybackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.config.CosClientConfig;
import com.xmz.stellaragallerybackend.constant.UserConstant;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.manager.CosManager;
import com.xmz.stellaragallerybackend.mapper.PictureFavoriteMapper;
import com.xmz.stellaragallerybackend.mapper.PictureLikeMapper;
import com.xmz.stellaragallerybackend.mapper.PictureMapper;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureBatchReviewRequest;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureQueryRequest;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureReviewRequest;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureUpdateRequest;
import com.xmz.stellaragallerybackend.model.entity.Category;
import com.xmz.stellaragallerybackend.model.entity.Picture;
import com.xmz.stellaragallerybackend.model.entity.PictureFavorite;
import com.xmz.stellaragallerybackend.model.entity.PictureLike;
import com.xmz.stellaragallerybackend.model.entity.Space;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.model.vo.PictureVO;
import com.xmz.stellaragallerybackend.service.CategoryService;
import com.xmz.stellaragallerybackend.service.PictureService;
import com.xmz.stellaragallerybackend.service.SpaceService;
import com.xmz.stellaragallerybackend.service.TagService;
import com.xmz.stellaragallerybackend.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
/**
 * 图片业务服务实现。
 */
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    /**
     * 公开图片。
     */
    private static final int PUBLIC = 1;

    /**
     * 私有图片。
     */
    private static final int PRIVATE = 0;

    /**
     * 审核待处理。
     */
    private static final int REVIEWING = 0;

    /**
     * 审核通过。
     */
    private static final int REVIEW_PASS = 1;

    /**
     * 审核拒绝。
     */
    private static final int REVIEW_REJECT = 2;

    /**
     * 图片上传管理器。
     */
    private final CosManager cosManager;

    /**
     * COS 配置，用于拼接上传目录。
     */
    private final CosClientConfig cosClientConfig;

    /**
     * 空间服务。
     */
    private final SpaceService spaceService;

    /**
     * 用户服务。
     */
    private final UserService userService;

    /**
     * 分类服务。
     */
    private final CategoryService categoryService;

    /**
     * 标签服务。
     */
    private final TagService tagService;

    /**
     * 点赞 Mapper。
     */
    private final PictureLikeMapper pictureLikeMapper;

    /**
     * 收藏 Mapper。
     */
    private final PictureFavoriteMapper pictureFavoriteMapper;

    /**
     * Redis 模板，用于 HyperLogLog 统计去重浏览。
     */
    private final StringRedisTemplate stringRedisTemplate;

    public PictureServiceImpl(CosManager cosManager,
                              CosClientConfig cosClientConfig,
                              SpaceService spaceService,
                              UserService userService,
                              CategoryService categoryService,
                              TagService tagService,
                              PictureLikeMapper pictureLikeMapper,
                              PictureFavoriteMapper pictureFavoriteMapper,
                              StringRedisTemplate stringRedisTemplate) {
        this.cosManager = cosManager;
        this.cosClientConfig = cosClientConfig;
        this.spaceService = spaceService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.pictureLikeMapper = pictureLikeMapper;
        this.pictureFavoriteMapper = pictureFavoriteMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 上传图片到 COS，并保存数据库元信息。
     */
    @Override
    public PictureVO uploadPicture(MultipartFile file, String name, String introduction, Long categoryId, List<String> tags, Integer isPublic) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(name), ErrorCode.PARAMS_ERROR, "图片名称不能为空");
        Long loginUserId = StpUtil.getLoginIdAsLong();
        Space personalSpace = spaceService.getOrCreatePersonalSpace(loginUserId);
        ThrowUtils.throwIf(ObjUtil.equal(personalSpace.getSpaceStatus(), 1), ErrorCode.FORBIDDEN_ERROR, "空间已被禁用");
        ThrowUtils.throwIf(personalSpace.getTotalSize() + file.getSize() > personalSpace.getMaxSize(),
                ErrorCode.OPERATION_ERROR, "空间容量不足");
        ThrowUtils.throwIf(personalSpace.getTotalCount() >= personalSpace.getMaxCount(),
                ErrorCode.OPERATION_ERROR, "空间图片数量已达上限");

        String format = StrUtil.blankToDefault(FileUtil.extName(file.getOriginalFilename()), "jpg").toLowerCase();
        validateFormat(format);
        ImageMeta imageMeta = readImageMeta(file);
        String key = buildObjectKey(loginUserId, format);
        String url = cosManager.uploadFile(key, file);
        List<String> normalizedTags = normalizeTags(tags);

        Picture picture = new Picture();
        picture.setUrl(url);
        picture.setThumbnailUrl(url);
        picture.setName(name.trim());
        picture.setIntroduction(StrUtil.blankToDefault(introduction, ""));
        picture.setCategoryId(categoryId);
        picture.setTags(JSONUtil.toJsonStr(normalizedTags));
        picture.setPicSize(file.getSize());
        picture.setPicWidth(imageMeta.width());
        picture.setPicHeight(imageMeta.height());
        picture.setPicScale(imageMeta.scale());
        picture.setPicFormat(format);
        picture.setPicColor("#7b61ff");
        picture.setUserId(loginUserId);
        picture.setSpaceId(personalSpace.getId());
        picture.setIsPublic(ObjUtil.equal(isPublic, PUBLIC) ? PUBLIC : PRIVATE);
        picture.setReviewStatus(ObjUtil.equal(picture.getIsPublic(), PUBLIC) ? REVIEWING : REVIEW_PASS);
        picture.setViewCount(0);
        picture.setLikeCount(0);
        picture.setFavoriteCount(0);
        picture.setDownloadCount(0);
        boolean saved = this.save(picture);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR);
        spaceService.updateSpaceUsage(personalSpace.getId(), file.getSize(), 1);
        tagService.recordTagUsage(normalizedTags);
        return getPictureVO(picture);
    }

    /**
     * 获取图片详情并记录去重浏览。
     */
    @Override
    public PictureVO getPictureVOById(Long id, String visitorKey) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        checkViewPermission(picture);
        recordView(picture, visitorKey);
        return getPictureVO(this.getById(id));
    }

    /**
     * 公共图库分页查询。
     */
    @Override
    public Page<PictureVO> listPublicPictureByPage(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = getQueryWrapper(pictureQueryRequest);
        queryWrapper.eq("is_public", PUBLIC).eq("review_status", REVIEW_PASS);
        return pageToVO(pagePicture(pictureQueryRequest, queryWrapper));
    }

    /**
     * 当前登录用户图片分页查询。
     */
    @Override
    public Page<PictureVO> listMyPictureByPage(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = getQueryWrapper(pictureQueryRequest);
        queryWrapper.eq("user_id", StpUtil.getLoginIdAsLong());
        return pageToVO(pagePicture(pictureQueryRequest, queryWrapper));
    }

    /**
     * 管理员图片分页查询。
     */
    @Override
    public Page<PictureVO> listAdminPictureByPage(PictureQueryRequest pictureQueryRequest) {
        return pageToVO(pagePicture(pictureQueryRequest, getQueryWrapper(pictureQueryRequest)));
    }

    /**
     * 当前登录用户收藏图片分页。
     */
    @Override
    public Page<PictureVO> listMyFavoritePictureByPage(PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest == null ? 1 : pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest == null ? 12 : pictureQueryRequest.getPageSize();
        List<Long> pictureIds = pictureFavoriteMapper.selectList(new QueryWrapper<PictureFavorite>()
                        .eq("user_id", StpUtil.getLoginIdAsLong())
                        .orderByDesc("create_time"))
                .stream()
                .map(PictureFavorite::getPictureId)
                .toList();
        Page<PictureVO> pictureVOPage = new Page<>(current, pageSize, 0);
        if (CollUtil.isEmpty(pictureIds)) {
            pictureVOPage.setRecords(Collections.emptyList());
            return pictureVOPage;
        }
        QueryWrapper<Picture> queryWrapper = getQueryWrapper(pictureQueryRequest);
        queryWrapper.in("id", pictureIds)
                .eq("is_public", PUBLIC)
                .eq("review_status", REVIEW_PASS);
        return pageToVO(this.page(new Page<>(current, pageSize), queryWrapper));
    }

    /**
     * 编辑图片信息。
     */
    @Override
    public Boolean updatePicture(PictureUpdateRequest pictureUpdateRequest) {
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() == null || pictureUpdateRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        Picture oldPicture = this.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        checkManagePermission(oldPicture);
        Picture updatePicture = new Picture();
        updatePicture.setId(oldPicture.getId());
        updatePicture.setName(StrUtil.blankToDefault(pictureUpdateRequest.getName(), oldPicture.getName()).trim());
        updatePicture.setIntroduction(StrUtil.blankToDefault(pictureUpdateRequest.getIntroduction(), ""));
        updatePicture.setCategoryId(pictureUpdateRequest.getCategoryId());
        List<String> normalizedTags = normalizeTags(pictureUpdateRequest.getTags());
        updatePicture.setTags(JSONUtil.toJsonStr(normalizedTags));
        if (pictureUpdateRequest.getIsPublic() != null) {
            updatePicture.setIsPublic(ObjUtil.equal(pictureUpdateRequest.getIsPublic(), PUBLIC) ? PUBLIC : PRIVATE);
            updatePicture.setReviewStatus(ObjUtil.equal(updatePicture.getIsPublic(), PUBLIC) ? REVIEWING : REVIEW_PASS);
            updatePicture.setReviewMessage(null);
            updatePicture.setReviewerId(null);
            updatePicture.setReviewTime(null);
        }
        boolean updated = this.updateById(updatePicture);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        tagService.recordTagUsage(normalizedTags);
        return true;
    }

    /**
     * 删除图片并扣减空间容量。
     */
    @Override
    public Boolean deletePicture(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        checkManagePermission(picture);
        boolean removed = this.removeById(id);
        ThrowUtils.throwIf(!removed, ErrorCode.OPERATION_ERROR);
        spaceService.updateSpaceUsage(picture.getSpaceId(), -safeLong(picture.getPicSize()), -1);
        return true;
    }

    /**
     * 审核单张图片。
     */
    @Override
    public Boolean reviewPicture(PictureReviewRequest pictureReviewRequest) {
        ThrowUtils.throwIf(pictureReviewRequest == null || pictureReviewRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureReviewRequest.getReviewStatus() == null
                        || (pictureReviewRequest.getReviewStatus() != REVIEW_PASS && pictureReviewRequest.getReviewStatus() != REVIEW_REJECT),
                ErrorCode.PARAMS_ERROR, "审核状态不合法");
        Picture picture = this.getById(pictureReviewRequest.getId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        Picture updatePicture = new Picture();
        updatePicture.setId(picture.getId());
        updatePicture.setReviewStatus(pictureReviewRequest.getReviewStatus());
        updatePicture.setReviewMessage(StrUtil.blankToDefault(pictureReviewRequest.getReviewMessage(),
                pictureReviewRequest.getReviewStatus() == REVIEW_PASS ? "审核通过" : "审核拒绝"));
        updatePicture.setReviewerId(StpUtil.getLoginIdAsLong());
        updatePicture.setReviewTime(LocalDateTime.now());
        boolean updated = this.updateById(updatePicture);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 批量审核图片。
     */
    @Override
    public Boolean batchReviewPicture(PictureBatchReviewRequest pictureBatchReviewRequest) {
        ThrowUtils.throwIf(pictureBatchReviewRequest == null || CollUtil.isEmpty(pictureBatchReviewRequest.getIds()),
                ErrorCode.PARAMS_ERROR);
        pictureBatchReviewRequest.getIds().forEach(id -> {
            PictureReviewRequest reviewRequest = new PictureReviewRequest();
            reviewRequest.setId(id);
            reviewRequest.setReviewStatus(pictureBatchReviewRequest.getReviewStatus());
            reviewRequest.setReviewMessage(pictureBatchReviewRequest.getReviewMessage());
            reviewPicture(reviewRequest);
        });
        return true;
    }

    /**
     * 点赞或取消点赞。
     */
    @Override
    public Boolean toggleLike(Long pictureId) {
        Picture picture = getVisiblePicture(pictureId);
        Long loginUserId = StpUtil.getLoginIdAsLong();
        QueryWrapper<PictureLike> queryWrapper = new QueryWrapper<PictureLike>()
                .eq("picture_id", picture.getId())
                .eq("user_id", loginUserId);
        PictureLike exists = pictureLikeMapper.selectOne(queryWrapper);
        if (exists == null) {
            PictureLike like = new PictureLike();
            like.setPictureId(picture.getId());
            like.setUserId(loginUserId);
            pictureLikeMapper.insert(like);
            this.lambdaUpdate().eq(Picture::getId, picture.getId()).setSql("like_count = like_count + 1").update();
            return true;
        }
        pictureLikeMapper.deleteById(exists.getId());
        this.lambdaUpdate().eq(Picture::getId, picture.getId()).setSql("like_count = GREATEST(like_count - 1, 0)").update();
        return false;
    }

    /**
     * 收藏或取消收藏。
     */
    @Override
    public Boolean toggleFavorite(Long pictureId) {
        Picture picture = getVisiblePicture(pictureId);
        Long loginUserId = StpUtil.getLoginIdAsLong();
        QueryWrapper<PictureFavorite> queryWrapper = new QueryWrapper<PictureFavorite>()
                .eq("picture_id", picture.getId())
                .eq("user_id", loginUserId);
        PictureFavorite exists = pictureFavoriteMapper.selectOne(queryWrapper);
        if (exists == null) {
            PictureFavorite favorite = new PictureFavorite();
            favorite.setPictureId(picture.getId());
            favorite.setUserId(loginUserId);
            pictureFavoriteMapper.insert(favorite);
            this.lambdaUpdate().eq(Picture::getId, picture.getId()).setSql("favorite_count = favorite_count + 1").update();
            return true;
        }
        pictureFavoriteMapper.deleteById(exists.getId());
        this.lambdaUpdate().eq(Picture::getId, picture.getId()).setSql("favorite_count = GREATEST(favorite_count - 1, 0)").update();
        return false;
    }

    /**
     * 下载图片。
     */
    @Override
    public String downloadPicture(Long id) {
        Picture picture = getVisiblePicture(id);
        this.lambdaUpdate().eq(Picture::getId, id).setSql("download_count = download_count + 1").update();
        return picture.getUrl();
    }

    /**
     * 构造图片查询条件。
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            queryWrapper.orderByDesc("create_time");
            return queryWrapper;
        }
        String keyword = pictureQueryRequest.getKeyword();
        if (StrUtil.isNotBlank(keyword)) {
            String keywordValue = keyword.trim();
            queryWrapper.and(wrapper -> wrapper.like("name", keywordValue).or().like("introduction", keywordValue));
        }
        queryWrapper.eq(pictureQueryRequest.getCategoryId() != null, "category_id", pictureQueryRequest.getCategoryId());
        queryWrapper.like(StrUtil.isNotBlank(pictureQueryRequest.getTag()), "tags", pictureQueryRequest.getTag());
        queryWrapper.eq(pictureQueryRequest.getUserId() != null, "user_id", pictureQueryRequest.getUserId());
        queryWrapper.eq(pictureQueryRequest.getSpaceId() != null, "space_id", pictureQueryRequest.getSpaceId());
        queryWrapper.eq(pictureQueryRequest.getIsPublic() != null, "is_public", pictureQueryRequest.getIsPublic());
        queryWrapper.eq(pictureQueryRequest.getReviewStatus() != null, "review_status", pictureQueryRequest.getReviewStatus());
        String sortField = normalizeSortField(pictureQueryRequest.getSortField());
        queryWrapper.orderBy(true, "ascend".equals(pictureQueryRequest.getSortOrder()), sortField);
        return queryWrapper;
    }

    /**
     * 转换为图片视图对象。
     */
    @Override
    public PictureVO getPictureVO(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = BeanUtil.copyProperties(picture, PictureVO.class);
        pictureVO.setTags(parseTags(picture.getTags()));
        Category category = picture.getCategoryId() == null ? null : categoryService.getById(picture.getCategoryId());
        pictureVO.setCategoryName(category == null ? null : category.getName());
        User user = picture.getUserId() == null ? null : userService.getById(picture.getUserId());
        pictureVO.setUserName(user == null ? null : user.getUserName());
        pictureVO.setUserAvatar(user == null ? null : user.getUserAvatar());
        if (StpUtil.isLogin()) {
            Long loginUserId = StpUtil.getLoginIdAsLong();
            pictureVO.setLiked(pictureLikeMapper.selectCount(new QueryWrapper<PictureLike>()
                    .eq("picture_id", picture.getId())
                    .eq("user_id", loginUserId)) > 0);
            pictureVO.setFavorited(pictureFavoriteMapper.selectCount(new QueryWrapper<PictureFavorite>()
                    .eq("picture_id", picture.getId())
                    .eq("user_id", loginUserId)) > 0);
        } else {
            pictureVO.setLiked(false);
            pictureVO.setFavorited(false);
        }
        return pictureVO;
    }

    /**
     * 分页查询图片实体。
     */
    private Page<Picture> pagePicture(PictureQueryRequest pictureQueryRequest, QueryWrapper<Picture> queryWrapper) {
        long current = pictureQueryRequest == null ? 1 : pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest == null ? 12 : pictureQueryRequest.getPageSize();
        return this.page(new Page<>(current, pageSize), queryWrapper);
    }

    /**
     * 图片实体分页转视图分页。
     */
    private Page<PictureVO> pageToVO(Page<Picture> picturePage) {
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        pictureVOPage.setRecords(picturePage.getRecords().stream().map(this::getPictureVO).toList());
        return pictureVOPage;
    }

    /**
     * 从图片文件读取宽高等基础元信息。
     */
    private ImageMeta readImageMeta(MultipartFile file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                return new ImageMeta(null, null, null);
            }
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            return new ImageMeta(width, height, height == 0 ? null : (double) width / height);
        } catch (IOException e) {
            return new ImageMeta(null, null, null);
        }
    }

    /**
     * 构造 COS 对象 Key。
     */
    private String buildObjectKey(Long userId, String format) {
        return StrUtil.removeSuffix(cosClientConfig.getUploadPrefix(), "/")
                + "/picture/" + userId + "/" + LocalDate.now() + "/" + IdUtil.fastSimpleUUID() + "." + format;
    }

    /**
     * 校验上传图片格式。
     */
    private void validateFormat(String format) {
        ThrowUtils.throwIf(!List.of("jpg", "jpeg", "png", "webp", "gif").contains(format),
                ErrorCode.PARAMS_ERROR, "仅支持 jpg、jpeg、png、webp、gif 图片");
    }

    /**
     * 标准化标签列表。
     */
    private List<String> normalizeTags(List<String> tags) {
        if (CollUtil.isEmpty(tags)) {
            return Collections.emptyList();
        }
        return tags.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .filter(tag -> tag.length() <= 24)
                .distinct()
                .limit(8)
                .toList();
    }

    /**
     * 解析数据库中的标签 JSON。
     */
    private List<String> parseTags(String tags) {
        if (StrUtil.isBlank(tags)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(tags, String.class);
        } catch (Exception e) {
            return List.of(tags);
        }
    }

    /**
     * 校验图片可见性。
     */
    private void checkViewPermission(Picture picture) {
        if (isPublicVisible(picture)) {
            return;
        }
        ThrowUtils.throwIf(!StpUtil.isLogin(), ErrorCode.NO_AUTH_ERROR);
        Long loginUserId = StpUtil.getLoginIdAsLong();
        boolean owner = loginUserId.equals(picture.getUserId());
        boolean admin = StpUtil.hasRole(UserConstant.ADMIN_ROLE);
        ThrowUtils.throwIf(!owner && !admin, ErrorCode.NO_AUTH_ERROR);
    }

    /**
     * 校验图片管理权限。
     */
    private void checkManagePermission(Picture picture) {
        Long loginUserId = StpUtil.getLoginIdAsLong();
        boolean owner = loginUserId.equals(picture.getUserId());
        boolean admin = StpUtil.hasRole(UserConstant.ADMIN_ROLE);
        ThrowUtils.throwIf(!owner && !admin, ErrorCode.NO_AUTH_ERROR);
    }

    /**
     * 获取当前用户可见图片。
     */
    private Picture getVisiblePicture(Long pictureId) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = this.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        checkViewPermission(picture);
        return picture;
    }

    /**
     * 判断图片是否对公共图库可见。
     */
    private boolean isPublicVisible(Picture picture) {
        return picture != null && ObjUtil.equal(picture.getIsPublic(), PUBLIC) && ObjUtil.equal(picture.getReviewStatus(), REVIEW_PASS);
    }

    /**
     * 记录 HyperLogLog 去重浏览量。
     */
    private void recordView(Picture picture, String visitorKey) {
        if (picture == null || StrUtil.isBlank(visitorKey)) {
            return;
        }
        try {
            String redisKey = "picture:view:hll:" + picture.getId();
            stringRedisTemplate.opsForHyperLogLog().add(redisKey, visitorKey);
            Long viewCount = stringRedisTemplate.opsForHyperLogLog().size(redisKey);
            if (viewCount != null && viewCount > safeInt(picture.getViewCount())) {
                Picture updatePicture = new Picture();
                updatePicture.setId(picture.getId());
                updatePicture.setViewCount(viewCount.intValue());
                this.updateById(updatePicture);
            }
        } catch (Exception ignored) {
            // 浏览统计是辅助数据，Redis 短暂异常不能影响图片详情展示。
        }
    }

    /**
     * 统一排序字段，避免直接使用任意前端字段。
     */
    private String normalizeSortField(String sortField) {
        if (StrUtil.isBlank(sortField)) {
            return "create_time";
        }
        return switch (sortField) {
            case "viewCount" -> "view_count";
            case "likeCount" -> "like_count";
            case "favoriteCount" -> "favorite_count";
            case "downloadCount" -> "download_count";
            case "updateTime" -> "update_time";
            default -> "create_time";
        };
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 图片元信息载体。
     */
    private record ImageMeta(Integer width, Integer height, Double scale) {
    }
}

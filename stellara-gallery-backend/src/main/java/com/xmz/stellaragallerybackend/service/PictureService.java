package com.xmz.stellaragallerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureBatchReviewRequest;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureQueryRequest;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureReviewRequest;
import com.xmz.stellaragallerybackend.model.dto.picture.PictureUpdateRequest;
import com.xmz.stellaragallerybackend.model.entity.Picture;
import com.xmz.stellaragallerybackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 图片业务服务。
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片到 COS 并保存元信息。
     */
    PictureVO uploadPicture(MultipartFile file, String name, String introduction, Long categoryId, List<String> tags, Integer isPublic);

    /**
     * 根据权限获取图片详情。
     */
    PictureVO getPictureVOById(Long id, String visitorKey);

    /**
     * 公共图库分页。
     */
    Page<PictureVO> listPublicPictureByPage(PictureQueryRequest pictureQueryRequest);

    /**
     * 我的空间图片分页。
     */
    Page<PictureVO> listMyPictureByPage(PictureQueryRequest pictureQueryRequest);

    /**
     * 管理员图片分页。
     */
    Page<PictureVO> listAdminPictureByPage(PictureQueryRequest pictureQueryRequest);

    /**
     * 收藏夹分页。
     */
    Page<PictureVO> listMyFavoritePictureByPage(PictureQueryRequest pictureQueryRequest);

    /**
     * 编辑图片基础信息。
     */
    Boolean updatePicture(PictureUpdateRequest pictureUpdateRequest);

    /**
     * 删除图片。
     */
    Boolean deletePicture(Long id);

    /**
     * 审核图片。
     */
    Boolean reviewPicture(PictureReviewRequest pictureReviewRequest);

    /**
     * 批量审核图片。
     */
    Boolean batchReviewPicture(PictureBatchReviewRequest pictureBatchReviewRequest);

    /**
     * 点赞或取消点赞。
     */
    Boolean toggleLike(Long pictureId);

    /**
     * 收藏或取消收藏。
     */
    Boolean toggleFavorite(Long pictureId);

    /**
     * 下载图片并累加下载次数。
     */
    String downloadPicture(Long id);

    /**
     * 构造查询条件。
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 转换为图片视图对象。
     */
    PictureVO getPictureVO(Picture picture);
}

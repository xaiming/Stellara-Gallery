package com.xmz.stellaragallerybackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.mapper.TagMapper;
import com.xmz.stellaragallerybackend.model.dto.tag.TagRequest;
import com.xmz.stellaragallerybackend.model.entity.Tag;
import com.xmz.stellaragallerybackend.model.vo.TagVO;
import com.xmz.stellaragallerybackend.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * 标签业务服务实现。
 */
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    /**
     * 默认标签颜色。
     */
    private static final String DEFAULT_COLOR = "#7b61ff";

    /**
     * 新增标签。
     */
    @Override
    public Long addTag(TagRequest tagRequest) {
        validTag(tagRequest, true);
        String name = tagRequest.getName().trim();
        boolean exists = this.lambdaQuery().eq(Tag::getName, name).exists();
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "标签名称已存在");
        Tag tag = BeanUtil.copyProperties(tagRequest, Tag.class);
        tag.setName(name);
        tag.setColor(StrUtil.blankToDefault(tag.getColor(), DEFAULT_COLOR));
        tag.setUseCount(0);
        boolean saved = this.save(tag);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR);
        return tag.getId();
    }

    /**
     * 更新标签。
     */
    @Override
    public Boolean updateTag(TagRequest tagRequest) {
        validTag(tagRequest, false);
        Tag tag = BeanUtil.copyProperties(tagRequest, Tag.class);
        if (StrUtil.isNotBlank(tag.getName())) {
            tag.setName(tag.getName().trim());
        }
        boolean updated = this.updateById(tag);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 查询标签列表。
     */
    @Override
    public List<TagVO> listTag() {
        return this.lambdaQuery()
                .orderByDesc(Tag::getUseCount)
                .orderByDesc(Tag::getCreateTime)
                .list()
                .stream()
                .map(this::getTagVO)
                .toList();
    }

    /**
     * 确保上传图片时填写的标签可被后台管理。
     */
    @Override
    public void recordTagUsage(List<String> tags) {
        if (CollUtil.isEmpty(tags)) {
            return;
        }
        tags.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .forEach(name -> {
                    Tag tag = this.lambdaQuery().eq(Tag::getName, name).one();
                    if (tag == null) {
                        Tag newTag = new Tag();
                        newTag.setName(name);
                        newTag.setColor(DEFAULT_COLOR);
                        newTag.setUseCount(1);
                        this.save(newTag);
                    } else {
                        this.lambdaUpdate()
                                .eq(Tag::getId, tag.getId())
                                .setSql("use_count = use_count + 1")
                                .update();
                    }
                });
    }

    /**
     * 转换为标签视图对象。
     */
    @Override
    public TagVO getTagVO(Tag tag) {
        return tag == null ? null : BeanUtil.copyProperties(tag, TagVO.class);
    }

    /**
     * 校验标签字段。
     */
    private void validTag(TagRequest tagRequest, boolean add) {
        ThrowUtils.throwIf(tagRequest == null, ErrorCode.PARAMS_ERROR);
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(tagRequest.getName()), ErrorCode.PARAMS_ERROR, "标签名称不能为空");
        } else {
            ThrowUtils.throwIf(tagRequest.getId() == null || tagRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isNotBlank(tagRequest.getName())) {
            ThrowUtils.throwIf(tagRequest.getName().trim().length() > 64, ErrorCode.PARAMS_ERROR, "标签名称过长");
        }
    }
}

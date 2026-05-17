package com.xmz.stellaragallerybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmz.stellaragallerybackend.model.dto.tag.TagRequest;
import com.xmz.stellaragallerybackend.model.entity.Tag;
import com.xmz.stellaragallerybackend.model.vo.TagVO;

import java.util.List;

/**
 * 标签业务服务。
 */
public interface TagService extends IService<Tag> {

    /**
     * 新增标签。
     */
    Long addTag(TagRequest tagRequest);

    /**
     * 更新标签。
     */
    Boolean updateTag(TagRequest tagRequest);

    /**
     * 查询标签列表。
     */
    List<TagVO> listTag();

    /**
     * 确保标签存在并累加使用次数。
     */
    void recordTagUsage(List<String> tags);

    /**
     * 转换为标签视图对象。
     */
    TagVO getTagVO(Tag tag);
}

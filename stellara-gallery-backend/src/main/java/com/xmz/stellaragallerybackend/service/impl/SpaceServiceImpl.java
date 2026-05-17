package com.xmz.stellaragallerybackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.mapper.SpaceMapper;
import com.xmz.stellaragallerybackend.model.entity.Space;
import com.xmz.stellaragallerybackend.model.vo.SpaceVO;
import com.xmz.stellaragallerybackend.service.SpaceService;
import org.springframework.stereotype.Service;

@Service
/**
 * 空间业务服务实现。
 */
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    /**
     * 默认个人空间最大容量：1GB。
     */
    private static final long DEFAULT_MAX_SIZE = 1024L * 1024L * 1024L;

    /**
     * 默认个人空间最大图片数。
     */
    private static final int DEFAULT_MAX_COUNT = 1000;

    /**
     * 获取或创建用户个人空间。
     */
    @Override
    public Space getOrCreatePersonalSpace(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR);
        Space exists = this.lambdaQuery()
                .eq(Space::getUserId, userId)
                .eq(Space::getSpaceType, 0)
                .one();
        if (exists != null) {
            return exists;
        }
        Space space = new Space();
        space.setSpaceName("我的星璃空间");
        space.setSpaceType(0);
        space.setSpaceLevel(0);
        space.setUserId(userId);
        space.setMaxSize(DEFAULT_MAX_SIZE);
        space.setMaxCount(DEFAULT_MAX_COUNT);
        space.setTotalSize(0L);
        space.setTotalCount(0);
        space.setSpaceStatus(0);
        boolean saved = this.save(space);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "个人空间创建失败");
        return space;
    }

    /**
     * 更新空间容量与图片数量。
     */
    @Override
    public void updateSpaceUsage(Long spaceId, long sizeDelta, int countDelta) {
        if (spaceId == null || spaceId <= 0) {
            return;
        }
        Space space = this.getById(spaceId);
        if (space == null) {
            return;
        }
        Space updateSpace = new Space();
        updateSpace.setId(spaceId);
        updateSpace.setTotalSize(Math.max(0L, safeLong(space.getTotalSize()) + sizeDelta));
        updateSpace.setTotalCount(Math.max(0, safeInt(space.getTotalCount()) + countDelta));
        this.updateById(updateSpace);
    }

    /**
     * 获取当前登录用户个人空间。
     */
    @Override
    public SpaceVO getMySpace() {
        return getSpaceVO(getOrCreatePersonalSpace(StpUtil.getLoginIdAsLong()));
    }

    /**
     * 转换为空间视图对象。
     */
    @Override
    public SpaceVO getSpaceVO(Space space) {
        return space == null ? null : BeanUtil.copyProperties(space, SpaceVO.class);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}

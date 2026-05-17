package com.xmz.stellaragallerybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmz.stellaragallerybackend.model.entity.Space;
import com.xmz.stellaragallerybackend.model.vo.SpaceVO;

/**
 * 空间业务服务。
 */
public interface SpaceService extends IService<Space> {

    /**
     * 获取或创建用户默认个人空间。
     */
    Space getOrCreatePersonalSpace(Long userId);

    /**
     * 更新空间容量统计。
     */
    void updateSpaceUsage(Long spaceId, long sizeDelta, int countDelta);

    /**
     * 获取当前登录用户个人空间。
     */
    SpaceVO getMySpace();

    /**
     * 转换为空间视图对象。
     */
    SpaceVO getSpaceVO(Space space);
}

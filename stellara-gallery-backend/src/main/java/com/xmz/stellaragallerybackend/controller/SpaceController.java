package com.xmz.stellaragallerybackend.controller;

import com.xmz.stellaragallerybackend.common.BaseResponse;
import com.xmz.stellaragallerybackend.common.ResultUtils;
import com.xmz.stellaragallerybackend.model.vo.SpaceVO;
import com.xmz.stellaragallerybackend.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "空间管理")
@RestController
@RequestMapping("/space")
/**
 * 空间接口控制器。
 */
public class SpaceController {

    /**
     * 空间业务服务。
     */
    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    /**
     * 获取当前登录用户的个人空间，不存在时自动创建。
     */
    @Operation(summary = "获取我的个人空间")
    @GetMapping("/my")
    public BaseResponse<SpaceVO> getMySpace() {
        return ResultUtils.success(spaceService.getMySpace());
    }
}

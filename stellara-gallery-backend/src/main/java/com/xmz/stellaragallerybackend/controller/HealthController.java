package com.xmz.stellaragallerybackend.controller;

import cn.hutool.core.date.DateUtil;
import com.xmz.stellaragallerybackend.common.BaseResponse;
import com.xmz.stellaragallerybackend.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "系统接口")
@RestController
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public BaseResponse<Map<String, Object>> health() {
        return ResultUtils.success(Map.of(
                "status", "ok",
                "application", "stellara-gallery-backend",
                "time", DateUtil.now()
        ));
    }
}

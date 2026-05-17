package com.xmz.stellaragallerybackend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.xmz.stellaragallerybackend.common.BaseResponse;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
/**
 * 全局异常处理器。
 */
public class GlobalExceptionHandler {

    /**
     * 处理项目主动抛出的业务异常。
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理 Sa-Token 未登录异常，统一返回项目约定的未登录错误码。
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginExceptionHandler(NotLoginException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, ErrorCode.NOT_LOGIN_ERROR.getMessage());
    }

    /**
     * 处理 Sa-Token 角色不足异常，统一返回无权限错误码。
     */
    @ExceptionHandler(NotRoleException.class)
    public BaseResponse<?> notRoleExceptionHandler(NotRoleException e) {
        log.error("NotRoleException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, ErrorCode.NO_AUTH_ERROR.getMessage());
    }

    /**
     * 处理上传文件超限异常，避免大图上传时返回系统错误。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public BaseResponse<?> maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException", e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "上传文件过大，单张图片最大 20MB");
    }

    /**
     * 兜底处理未预期的运行时异常，避免直接暴露默认错误页面。
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}

package com.xmz.stellaragallerybackend.common;

/**
 * 统一响应构造工具。
 */
public final class ResultUtils {

    private ResultUtils() {
    }

    /**
     * 构造成功响应。
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, ErrorCode.SUCCESS.getMessage());
    }

    /**
     * 按统一错误码构造失败响应。
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 按自定义状态码和文案构造失败响应。
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 使用统一错误码的 code，并覆盖默认错误文案。
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}

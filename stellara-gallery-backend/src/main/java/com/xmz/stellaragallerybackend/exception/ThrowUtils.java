package com.xmz.stellaragallerybackend.exception;

import com.xmz.stellaragallerybackend.common.ErrorCode;

/**
 * 条件抛异常工具类。
 */
public final class ThrowUtils {

    private ThrowUtils() {
    }

    /**
     * 条件成立时抛出指定运行时异常。
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立时根据统一错误码抛出业务异常。
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立时根据统一错误码和自定义文案抛出业务异常。
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}

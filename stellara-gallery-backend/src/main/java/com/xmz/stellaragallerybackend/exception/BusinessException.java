package com.xmz.stellaragallerybackend.exception;

import com.xmz.stellaragallerybackend.common.ErrorCode;
import lombok.Getter;

@Getter
/**
 * 项目业务异常。
 */
public class BusinessException extends RuntimeException {

    /**
     * 对应 ErrorCode 中定义的业务状态码。
     */
    private final int code;

    /**
     * 使用自定义状态码和错误文案创建业务异常。
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 直接使用统一错误码创建业务异常。
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用统一错误码的 code，并覆盖默认文案。
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}

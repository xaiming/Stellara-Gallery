package com.xmz.stellaragallerybackend.common;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 后端统一响应包装。
 */
public class BaseResponse<T> implements Serializable {

    /**
     * 业务状态码，0 表示成功，其它值来自 ErrorCode。
     */
    private int code;

    /**
     * 接口返回的数据主体。
     */
    private T data;

    /**
     * 给前端展示或调试使用的响应消息。
     */
    private String message;

    /**
     * 完整响应构造方法。
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 只携带状态码和数据时，默认消息为空。
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 根据统一错误码构造错误响应。
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

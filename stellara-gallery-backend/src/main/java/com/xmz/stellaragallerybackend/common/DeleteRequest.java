package com.xmz.stellaragallerybackend.common;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 通用删除请求体。
 */
public class DeleteRequest implements Serializable {

    /**
     * 要删除的数据 ID。
     */
    private Long id;
}

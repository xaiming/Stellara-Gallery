package com.xmz.stellaragallerybackend.common;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 分页查询基础请求体。
 */
public class PageRequest implements Serializable {

    /**
     * 当前页码，从 1 开始。
     */
    private long current = 1;

    /**
     * 每页记录数。
     */
    private long pageSize = 10;

    /**
     * 排序字段，对应数据库列名。
     */
    private String sortField;

    /**
     * 排序方向，兼容前端常见的 ascend / descend。
     */
    private String sortOrder = "descend";
}

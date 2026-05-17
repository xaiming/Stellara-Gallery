package com.xmz.stellaragallerybackend.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {

    private long current = 1;

    private long pageSize = 10;

    private String sortField;

    private String sortOrder = "descend";
}

package com.xmz.stellaragallerybackend.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    private Long id;
}

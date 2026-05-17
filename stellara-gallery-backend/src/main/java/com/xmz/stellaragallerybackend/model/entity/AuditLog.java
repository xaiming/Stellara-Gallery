package com.xmz.stellaragallerybackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户管理审计日志实体。
 */
@Data
@TableName("audit_log")
public class AuditLog implements Serializable {

    /**
     * 日志主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人用户 ID。
     */
    private Long operatorId;

    /**
     * 操作人账号。
     */
    private String operatorAccount;

    /**
     * 被操作用户 ID。
     */
    private Long targetUserId;

    /**
     * 操作类型。
     */
    private String action;

    /**
     * 操作说明。
     */
    private String detail;

    /**
     * 操作时间。
     */
    private LocalDateTime createTime;
}

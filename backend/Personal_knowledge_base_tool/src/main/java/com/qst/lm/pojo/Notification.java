package com.qst.lm.pojo;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 * 对应数据库表: notification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification")
public class Notification implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("collection_item_id")
    private Long collectionItemId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("is_read")
    private Integer isRead = 0;

    @TableField("remind_at")
    private LocalDateTime remindAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("read_at")
    private LocalDateTime readAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    @TableField("notify_type")
    private Integer notifyType;

}

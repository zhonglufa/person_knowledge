package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告触达统计实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("announcement_statistics")
public class AnnouncementStatistics implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("announcement_id")
    private Long announcementId;

    @TableField("view_count")
    private Integer viewCount = 0;

    @TableField("read_user_count")
    private Integer readUserCount = 0;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

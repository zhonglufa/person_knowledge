package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 搜索历史实体类
 * 对应数据库表: search_history
 */
@Data
@TableName("search_history")
public class SearchHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("keyword")
    private String keyword;

    @TableField("search_count")
    private Integer searchCount;

    @TableField("last_search_at")
    private LocalDateTime lastSearchAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

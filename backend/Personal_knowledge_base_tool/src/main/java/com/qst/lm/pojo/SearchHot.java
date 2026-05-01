package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 热门搜索实体类
 * 对应数据库表: search_hot
 */
@Data
@TableName("search_hot")
public class SearchHot {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("keyword")
    private String keyword;

    @TableField("search_total")
    private Long searchTotal;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏项标签关联实体类
 * 对应数据库表: collection_item_tag
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@TableName("collection_item_tag")
public class CollectionItemTag  implements Serializable {


    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收藏项ID
     */
    @TableField("collection_item_id")
    private Long collectionItemId;

    /**
     * 标签ID
     */
    @TableField("tag_id")
    private Long tagId;




}

package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏集实体类 - 用户可以创建不同的收藏集来组织收藏项
 * 对应数据库表: collection
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("collection")
@EqualsAndHashCode(callSuper = true)  //确保父类的字段也被纳入相等性比较，这对于数据库实体类尤其重要。
public class Collection extends BasePojo implements Serializable {



    /**
     * 收藏集ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收藏集名称
     */
    @TableField("name")
    private String name;

    /**
     * 收藏集描述
     */
    @TableField("description")
    private String description;

    /**
     * 所属用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收藏集封面图片URL
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 是否公开 (0=私有, 1=公开)
     */
    @TableField("is_public")
    private Integer isPublic = 0;

    /**
     * 是否默认收藏集 (0=否, 1=是)
     */
    @TableField("is_default")
    private Integer isDefault = 0;

    /**
     * 收藏项数量（非数据库字段，用于业务逻辑统计）
     * 注：仅统计未删除的收藏项（deleted=0）
     */
    @TableField(exist = false)
    private Integer itemCount = 0;

}

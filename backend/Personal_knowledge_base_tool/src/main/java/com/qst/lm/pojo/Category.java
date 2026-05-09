package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类实体类
 * 用于组织和管理用户的知识内容
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"},callSuper = true)//确保父类的字段也被纳入相等性比较，这对于数据库实体类尤其重要。
@TableName("category")
public class Category extends BasePojo implements Serializable {


    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    @TableField("name")
    private String name;


    /**
     * 父分类ID（用于构建分类树结构）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 所属用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 分类描述
     */
    @TableField("description")
    private String description;

    /**
     * 分类图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 分类颜色
     */
    @TableField("color")
    private String color;

}

package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签实体类
 * 对应数据库表: tag
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tag")
@EqualsAndHashCode(callSuper = true)  //确保父类的字段也被纳入相等性比较，这对于数据库实体类尤其重要。
public class Tag extends BasePojo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    @TableField("name")
    private String name;

    /**
     * 标签颜色
     */
    @TableField("color")
    private String color;

    /**
     * 用户ID（数据隔离）
     */
    @TableField("user_id")
    private Long userId;

}

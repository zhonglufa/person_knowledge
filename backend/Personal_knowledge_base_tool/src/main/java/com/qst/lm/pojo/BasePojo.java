package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础实体类，提供通用字段
 * 所有实体类都应该继承此类
 */
@Data
public class BasePojo{

    /**
     * 创建时间   fieldFill = FieldFill.INSERT自动填充和自动更新开关
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Integer deleted = 0;
}

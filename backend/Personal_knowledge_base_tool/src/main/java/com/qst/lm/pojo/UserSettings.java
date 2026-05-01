package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户个性化设置实体类
 */
@Data
@TableName("user_settings")
public class UserSettings {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 主题:light/dark
     */
    private String theme;

    /**
     * 通知偏好设置(JSON)
     */
    private String notifyPreferences;

    /**
     * 显示模式:grid/list
     */
    private String displayMode;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

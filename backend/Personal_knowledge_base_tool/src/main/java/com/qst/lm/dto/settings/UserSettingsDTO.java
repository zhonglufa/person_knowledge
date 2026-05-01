package com.qst.lm.dto.settings;

import lombok.Data;

import java.util.Map;

/**
 * 用户个性化设置请求DTO
 */
@Data
public class UserSettingsDTO {

    /**
     * 主题:light/dark
     */
    private String theme;

    /**
     * 通知偏好设置
     */
    private Map<String, Object> notifyPreferences;

    /**
     * 显示模式:grid/list
     */
    private String displayMode;
}

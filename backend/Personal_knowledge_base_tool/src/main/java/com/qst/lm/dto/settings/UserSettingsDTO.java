package com.qst.lm.dto.settings;

import lombok.Data;

import java.util.Map;

@Data
public class UserSettingsDTO {

    private String theme;

    private Map<String, Object> notifyPreferences;

    private String displayMode;

    private Map<String, Object> privacy;

    private String density;

    private String fontSize;
}

package com.qst.lm.dto.announcement;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告请求DTO
 */
@Data
public class AnnouncementDTO {

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    private String title;

    /**
     * 公告内容
     */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /**
     * 生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveAt;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireAt;

    /**
     * 公告类型: system=系统公告, activity=活动通知, maintenance=维护通知
     */
    private String type;

    /**
     * 优先级: low=低, medium=中, high=高, urgent=紧急
     */
    private String priority;
}

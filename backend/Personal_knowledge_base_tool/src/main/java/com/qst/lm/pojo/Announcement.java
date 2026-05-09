package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统公告实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcement")
public class Announcement extends BasePojo {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveAt;

    /**
     * 过期时间
     */
    private LocalDateTime expireAt;

    /**
     * 状态: 0=草稿, 1=已发布, 2=已下架, 3=已过期, 4=待定时发布
     */
    private Integer status;

    /**
     * 定时发布时间
     */
    private LocalDateTime scheduledAt;

    /**
     * 公告类型: system=系统公告, activity=活动通知, maintenance=维护通知
     */
    private String type;

    /**
     * 优先级: low=低, medium=中, high=高, urgent=紧急
     */
    private String priority;

    /**
     * 创建人(管理员ID)
     */
    private Long createdBy;
}

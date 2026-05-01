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
     * 状态:1=生效,0=下架
     */
    private Integer status;

    /**
     * 创建人(管理员ID)
     */
    private Long createdBy;
}

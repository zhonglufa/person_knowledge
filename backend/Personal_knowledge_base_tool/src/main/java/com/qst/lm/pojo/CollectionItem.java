package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


/**
 * 收藏项实体类 - 支持多类型内容收藏
 * 对应数据库表: collection_item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("collection_item")
@EqualsAndHashCode(callSuper = true)  //确保父类的字段也被纳入相等性比较，这对于数据库实体类尤其重要。
public class CollectionItem extends BasePojo implements Serializable {

    /**
     * 收藏项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 所属收藏集ID
     */
    @TableField("collection_id")
    private Long collectionId;

    /**
     * 来源类型: 1=网页, 2=图片, 3=文本, 4=视频, 5=笔记
     */
    @TableField("source_type")
    private Integer sourceType;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 来源URL
     */
    @TableField("source_url")
    private String sourceUrl;

    /**
     * 关联笔记ID
     */
    @TableField("note_id")
    private Long noteId;

    /**
     * 关联沉淀笔记ID
     */
    @TableField("related_note_id")
    private Long relatedNoteId;

    /**
     * 笔记来源展示文案
     */
    @TableField("note_source_label")
    private String noteSourceLabel;

    /**
     * 个人核心摘要(渐进加工)
     */
    @TableField("core_abstract")
    private String coreAbstract;

    /**
     * 关键词/标签
     */
    @TableField("keywords")
    private String keywords;

    /**
     * 学习目标
     */
    @TableField("study_goal")
    private String studyGoal;


    /**
     * 加工状态: undigest(未加工), digesting(加工中), digested(已加工), abandoned(已废弃)
     */
    @TableField("digest_status")
    private String digestStatus = "undigest";

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean isRead = false;

    /**
     * 是否星标
     */
    @TableField("is_star")
    private Boolean isStar = false;

    /**
     * 访问次数
     */
    @TableField("visit_count")
    private Integer visitCount = 0;


    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;


    /**
     * 来源
     */
    @TableField("source")
    private String source;


    /**
     * 学习进度
     */
    @TableField("study_progress")
    private String studyProgress;

    /**
     * 学习提醒时间
     */
    @TableField("remind_at")
    private LocalDateTime remindAt;



}

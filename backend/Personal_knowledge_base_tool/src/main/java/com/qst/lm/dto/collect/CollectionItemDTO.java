package com.qst.lm.dto.collect;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 收藏项创建/更新请求DTO
 */
@Data
public class CollectionItemDTO {

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 来源类型: 1=网页, 2=图片, 3=文本, 4=视频, 5=笔记
     */
    @NotNull(message = "来源类型不能为空")
    private Integer sourceType;

    /**
     * 所属收藏集ID
     */
    private Long collectionId;

    /**
     * 核心摘要
     */
    private String coreAbstract;

    /**
     * 消化状态
     */
    private String digestStatus;

    /**
     * 学习进度
     */
    private String studyProgress;

    /**
     * 学习目标
     */
    private String studyGoal;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 来源名称
     */
    private String source;

    /**
     * 来源URL
     */
    private String sourceUrl;

    /**
     * 关联笔记ID
     */
    private Long noteId;

    /**
     * 关联沉淀笔记ID
     */
    private Long relatedNoteId;

    /**
     * 笔记来源展示文案
     */
    private String noteSourceLabel;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 是否星标
     */
    private Boolean isStar;

    /**
     * 是否公开: 0=私密, 1=公开
     */
    private Integer isPublic;
}

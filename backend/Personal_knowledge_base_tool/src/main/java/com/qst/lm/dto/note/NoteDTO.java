package com.qst.lm.dto.note;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 笔记数据传输对象
 */
@Data
public class NoteDTO {

    /**
     * 笔记标题
     */
    @NotBlank(message = "笔记标题不能为空")
    private String title;

    /**
     * 笔记内容
     */
    @NotBlank(message = "笔记内容不能为空")
    private String content;

    /**
     * 笔记类型（conceptual/procedural/factual/metacognitive），默认conceptual
     */
    private String noteType = "conceptual";

    /**
     * 笔记描述
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 关联收藏项ID（可选）
     */
    private Long collectionItemId;

    /**
     * 是否公开，默认false
     */
    private Boolean isPublic = false;

    /**
     * 状态（草稿/完成）
     */
    private String status;
}

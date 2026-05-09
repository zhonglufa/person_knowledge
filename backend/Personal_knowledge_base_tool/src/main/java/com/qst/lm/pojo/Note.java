package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记实体类
 * 对应数据库表: note
 * 注意: note表时间字段为create_time/update_time，与BasePojo不同，因此不继承BasePojo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("note")
public class Note implements Serializable {

    /**
     * 笔记ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 关联的收藏项ID
     */
    @TableField("collection_item_id")
    private Long collectionItemId;

    /**
     * 笔记标题
     */
    @TableField("title")
    private String title;

    /**
     * 笔记类型 (conceptual=概念性, procedural=程序性, factual=事实性, metacognitive=元认知)
     */
    @TableField("note_type")
    private String noteType = "conceptual";

    /**
     * 笔记内容
     */
    @TableField("content")
    private String content;

    /**
     * 是否公开 (0=私有, 1=公开)
     */
    @TableField("is_public")
    private Integer isPublic = 0;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    /**
     * 笔记描述
     */
    @TableField("description")
    private String description;

    /**
     * 封面图片URL
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 完成时间（注意数据库字段名拼写为finshed_at）
     */
    @TableField("finshed_at")
    private LocalDateTime finishedAt;

    /**
     * 笔记状态：草稿/完成
     */
    @TableField("status")
    private String status = "草稿";

    /**
     * 浏览量
     */
    @TableField("views")
    private Integer views = 0;

    /**
     * 点赞数
     */
    @TableField("likes")
    private Integer likes = 0;

    /**
     * 热度评分 = views*0.6 + likes*0.4
     */
    @TableField("hot_score")
    private Double hotScore = 0.0;

    /**
     * 消化状态：undigest=未消化, digesting=消化中, digested=已消化
     * 注意：与collection_item表保持一致，使用undigest而非undigested
     */
    @TableField("digest_status")
    private String digestStatus = "undigest";

    /**
     * 笔记修订次数（闭环字段）
     */
    @TableField("update_count")
    private Integer updateCount = 0;

    /**
     * 复习次数（闭环字段）
     */
    @TableField("review_count")
    private Integer reviewCount = 0;

    /**
     * 字数统计（闭环字段）
     */
    @TableField("word_count")
    private Integer wordCount = 0;

    /**
     * 笔记质量评分（0-100）（闭环字段）
     */
    @TableField("note_quality_score")
    private Integer noteQualityScore = 0;

    /**
     * 来源收藏项信息（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private CollectionItem sourceCollectionItem;



}

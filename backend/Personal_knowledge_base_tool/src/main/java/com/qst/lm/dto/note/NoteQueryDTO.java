package com.qst.lm.dto.note;

import com.qst.lm.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 笔记查询数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoteQueryDTO extends PageDTO {

    /**
     * 笔记类型
     */
    private String noteType;

    /**
     * 笔记状态
     */
    private String status;

    /**
     * 是否公开
     */
    private Integer isPublic;

    /**
     * 搜索关键词
     */
    private String keyword;
}

package com.qst.lm.mapper;

import com.qst.lm.pojo.Note;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 笔记数据访问层
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    /**
     * 笔记列表带关联收藏项信息查询（XML实现）
     */
    List<Map<String, Object>> selectNoteWithItem(
            @Param("userId") Long userId,
            @Param("noteType") String noteType,
            @Param("status") String status,
            @Param("keyword") String keyword);
}

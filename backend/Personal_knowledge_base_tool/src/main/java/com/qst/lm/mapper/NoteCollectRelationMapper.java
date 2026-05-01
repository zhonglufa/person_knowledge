package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.UserNoteCollect;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记-收藏项关联数据访问层
 */
@Mapper
public interface NoteCollectRelationMapper extends BaseMapper<UserNoteCollect> {
}

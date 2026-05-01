package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.SearchHot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 热门搜索数据访问层
 */
@Mapper
public interface SearchHotMapper extends BaseMapper<SearchHot> {
}

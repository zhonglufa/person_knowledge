package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.ImportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 导入记录Mapper接口
 */
@Mapper
public interface ImportRecordMapper extends BaseMapper<ImportRecord> {

    /**
     * 查询用户的导入记录
     * @param userId 用户ID
     * @return 导入记录列表
     */
    @Select("SELECT * FROM import_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ImportRecord> selectByUserId(@Param("userId") Long userId);
}

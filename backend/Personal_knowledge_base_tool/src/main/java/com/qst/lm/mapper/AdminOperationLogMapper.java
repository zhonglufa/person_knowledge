package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员操作日志Mapper接口
 */
@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}

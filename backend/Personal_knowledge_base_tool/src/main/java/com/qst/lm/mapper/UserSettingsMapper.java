package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.UserSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户个性化设置Mapper接口
 */
@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettings> {

    /**
     * 根据用户ID查询设置
     * @param userId 用户ID
     * @return 用户设置对象
     */
    @Select("SELECT * FROM user_settings WHERE user_id = #{userId} LIMIT 1")
    UserSettings selectByUserId(@Param("userId") Long userId);
}

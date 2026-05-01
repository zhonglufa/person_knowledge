package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户角色关联Mapper接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 查询用户是否拥有指定角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 关联记录数
     */
    @Select("SELECT COUNT(*) FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int countByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 删除用户角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 删除记录数
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 删除用户的所有角色关联
     * @param userId 用户ID
     * @return 删除记录数
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}

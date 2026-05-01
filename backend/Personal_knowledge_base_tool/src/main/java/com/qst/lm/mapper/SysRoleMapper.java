package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色码查询角色
     * @param roleCode 角色码
     * @return 角色对象
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0 AND status = 1")
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);
}

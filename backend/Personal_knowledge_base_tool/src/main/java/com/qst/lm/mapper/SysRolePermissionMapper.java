package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色权限关联Mapper接口
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 查询角色是否拥有指定权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 关联记录数
     */
    @Select("SELECT COUNT(*) FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int countByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 删除角色权限关联
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 删除记录数
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 删除角色的所有权限关联
     * @param roleId 角色ID
     * @return 删除记录数
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}

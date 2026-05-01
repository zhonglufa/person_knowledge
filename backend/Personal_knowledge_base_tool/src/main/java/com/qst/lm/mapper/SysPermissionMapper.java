package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据用户ID查询权限码列表
     * @param userId 用户ID
     * @return 权限码列表
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限码查询权限
     * @param permissionCode 权限码
     * @return 权限对象
     */
    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode} AND deleted = 0 AND status = 1")
    SysPermission selectByPermissionCode(@Param("permissionCode") String permissionCode);
}

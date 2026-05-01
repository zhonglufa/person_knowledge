package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_permission")
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends BasePojo implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("permission_name")
    private String permissionName;

    @TableField("permission_type")
    private String permissionType;

    @TableField("module")
    private String module;

    @TableField("path")
    private String path;

    @TableField("method")
    private String method;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("status")
    private Integer status;

    @TableField(exist = false)
    private List<SysPermission> children;
}

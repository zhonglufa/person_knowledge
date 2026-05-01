package com.qst.lm.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员编辑用户专用DTO
 * 仅允许修改昵称和角色，不可修改用户名、邮箱、密码
 */
@Data
public class AdminEditUserDTO {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 角色（仅允许 commonUser 或 admin）
     */
    @NotBlank(message = "角色不能为空")
    private String role;
}

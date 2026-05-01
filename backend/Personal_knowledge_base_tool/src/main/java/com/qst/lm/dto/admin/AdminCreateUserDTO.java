package com.qst.lm.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员创建用户专用DTO
 * 不需要验证码，由管理员直接创建
 */
@Data
public class AdminCreateUserDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20位之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,20}$", message = "密码必须包含字母和数字，长度8-20位")
    private String password;

    /**
     * 昵称（可选）
     */
    private String nickname;

    /**
     * 角色（必填，仅允许 commonUser 或 admin）
     */
    @NotBlank(message = "角色不能为空")
    private String role;
}

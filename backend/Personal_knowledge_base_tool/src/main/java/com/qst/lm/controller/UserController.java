package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.auth.ChangePasswordDTO;
import com.qst.lm.dto.auth.LoginDTO;
import com.qst.lm.dto.auth.RegisterDTO;
import com.qst.lm.dto.settings.UserSettingsDTO;
import com.qst.lm.service.IUserService;
import com.qst.lm.service.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户认证模块", description = "用户认证相关接口")
public class UserController {

    private final IUserService userService;
    private final VerificationCodeService verificationCodeService;

    public UserController(IUserService userService, VerificationCodeService verificationCodeService) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录认证")
    public R authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用用户名、邮箱和密码进行用户注册")
    public R registerUser(@Valid @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    public R changePassword(@RequestAttribute Long userId,
                            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        return userService.updatePassword(userId, changePasswordDTO);
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-verify-code")
    @Operation(summary = "发送验证码", description = "向指定邮箱发送验证码")
    public R sendVerifyCode(@RequestBody LoginDTO loginDTO) {
        String email = loginDTO.getEmail();

        // 检查是否可以发送验证码
        if (!verificationCodeService.canSendCode(email)) {
            return R.error(429, "发送频率过高，请稍后再试");
        }

        // 生成验证码
        String code = verificationCodeService.generateCode();

        // 保存验证码
        verificationCodeService.saveCode(email, code);

        // 发送邮件
        verificationCodeService.sendVerificationCode(email, code);

        return R.success("验证码发送成功");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出")
    public R logout() {
        return R.success("登出成功");
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user-info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    public R getUserInfo(@RequestAttribute Long userId) {
        return userService.getUserInfo(userId);
    }

    /**
     * 获取个人详细信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取个人详细信息", description = "获取当前用户的详细个人信息")
    public R getProfile(@RequestAttribute Long userId) {
        return userService.getProfile(userId);
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新个人信息", description = "更新当前用户的个人信息（nickname, email等）")
    public R updateProfile(@RequestAttribute Long userId, @RequestBody Map<String, Object> profileMap) {
        return userService.updateProfile(userId, profileMap);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像")
    public R uploadAvatar(@RequestAttribute Long userId, @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(userId, file);
    }

    /**
     * 获取个人统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "个人统计", description = "获取当前用户的统计数据")
    public R getStatistics(@RequestAttribute Long userId) {
        return userService.getStatistics(userId);
    }

    /**
     * 重置密码（忘记密码）
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过邮箱验证码重置密码")
    public R resetPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        return userService.resetPassword(email, code, newPassword);
    }

    // ==================== 扩展接口 ====================

    /**
     * 获取个性化设置
     */
    @GetMapping("/settings")
    @Operation(summary = "获取个性化设置", description = "获取当前用户的个性化设置，包括主题、通知偏好、显示模式等")
    public R getUserSettings(@RequestAttribute Long userId) {
        log.info("用户[{}]获取个性化设置", userId);
        return userService.getSettings(userId);
    }

    /**
     * 更新个性化设置
     */
    @PutMapping("/settings")
    @Operation(summary = "更新个性化设置", description = "更新当前用户的个性化设置")
    public R updateUserSettings(@RequestAttribute Long userId,
                                @Valid @RequestBody UserSettingsDTO settingsDTO) {
        log.info("用户[{}]更新个性化设置", userId);
        return userService.updateSettings(userId, settingsDTO);
    }
}

package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.auth.ChangePasswordDTO;
import com.qst.lm.dto.auth.LoginDTO;
import com.qst.lm.dto.auth.RegisterDTO;
import com.qst.lm.dto.settings.UserSettingsDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface IUserService {

    /**
     * 用户登录
     *
     * @param dto 登录请求参数
     * @return 登录结果
     */
    R login(LoginDTO dto);

    /**
     * 用户注册
     *
     * @param dto 注册请求参数
     * @return 注册结果
     */
    R register(RegisterDTO dto);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    R getUserInfo(Long userId);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param dto    修改密码请求参数
     * @return 修改结果
     */
    R updatePassword(Long userId, ChangePasswordDTO dto);

    /**
     * 获取个人详细信息
     *
     * @param userId 用户ID
     * @return 个人详细信息
     */
    R getProfile(Long userId);

    /**
     * 更新个人信息
     *
     * @param userId    用户ID
     * @param profileMap 个人信息字段
     * @return 更新结果
     */
    R updateProfile(Long userId, Map<String, Object> profileMap);

    /**
     * 上传头像
     *
     * @param userId 用户ID
     * @param file   头像文件
     * @return 头像URL
     */
    R uploadAvatar(Long userId, MultipartFile file);

    /**
     * 获取个人统计数据
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    R getStatistics(Long userId);

    /**
     * 重置密码（忘记密码）
     *
     * @param email       邮箱
     * @param code        验证码
     * @param newPassword 新密码
     * @return 重置结果
     */
    R resetPassword(String email, String code, String newPassword);

    /**
     * 获取个性化设置
     *
     * @param userId 用户ID
     * @return 个性化设置
     */
    R getSettings(Long userId);

    /**
     * 更新个性化设置
     *
     * @param userId 用户ID
     * @param dto    设置信息
     * @return 更新结果
     */
    R updateSettings(Long userId, UserSettingsDTO dto);
}

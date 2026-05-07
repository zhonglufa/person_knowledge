package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.auth.ChangePasswordDTO;
import com.qst.lm.dto.auth.LoginDTO;
import com.qst.lm.dto.auth.RegisterDTO;
import com.qst.lm.dto.settings.UserSettingsDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IUserService {

    R login(LoginDTO dto);

    R register(RegisterDTO dto);

    R logout(String authorization);

    R getUserInfo(Long userId);

    R updatePassword(Long userId, ChangePasswordDTO dto);

    R getProfile(Long userId);

    R updateProfile(Long userId, Map<String, Object> profileMap);

    R uploadAvatar(Long userId, MultipartFile file);

    R getStatistics(Long userId);

    R resetPassword(String email, String code, String newPassword);

    R getSettings(Long userId);

    R updateSettings(Long userId, UserSettingsDTO dto);

    R sendVerifyCode(String email);
}

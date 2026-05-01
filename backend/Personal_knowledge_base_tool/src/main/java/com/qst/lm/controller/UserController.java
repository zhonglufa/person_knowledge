package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.auth.ChangePasswordDTO;
import com.qst.lm.dto.auth.LoginDTO;
import com.qst.lm.dto.auth.RegisterDTO;
import com.qst.lm.dto.settings.UserSettingsDTO;
import com.qst.lm.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public R authenticateUser(@Valid @RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    @PostMapping("/register")
    public R register(@Valid @RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/logout")
    public R logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return userService.logout(authorization);
    }

    @GetMapping("/info")
    public R getUserInfo(@RequestAttribute Long userId) {
        return userService.getUserInfo(userId);
    }

    @PutMapping("/password")
    public R changePassword(@RequestAttribute Long userId, @Valid @RequestBody ChangePasswordDTO dto) {
        return userService.updatePassword(userId, dto);
    }

    @GetMapping("/profile")
    public R getProfile(@RequestAttribute Long userId) {
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    public R updateProfile(@RequestAttribute Long userId, @RequestBody Map<String, Object> profileMap) {
        return userService.updateProfile(userId, profileMap);
    }

    @PostMapping("/avatar")
    public R uploadAvatar(@RequestAttribute Long userId, @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(userId, file);
    }

    @GetMapping("/statistics")
    public R getStatistics(@RequestAttribute Long userId) {
        return userService.getStatistics(userId);
    }

    @PostMapping("/reset-password")
    public R resetPassword(@RequestParam String email,
                           @RequestParam String code,
                           @RequestParam String newPassword) {
        return userService.resetPassword(email, code, newPassword);
    }

    @GetMapping("/settings")
    public R getSettings(@RequestAttribute Long userId) {
        return userService.getSettings(userId);
    }

    @PutMapping("/settings")
    public R updateSettings(@RequestAttribute Long userId, @RequestBody UserSettingsDTO dto) {
        return userService.updateSettings(userId, dto);
    }
}

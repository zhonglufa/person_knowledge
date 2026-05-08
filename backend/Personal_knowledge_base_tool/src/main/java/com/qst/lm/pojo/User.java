package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
@EqualsAndHashCode(callSuper = true)
public class User extends BasePojo implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("nickname")
    private String nickname;

    @TableField("avatar")
    private String avatar;

    @TableField("gender")
    private String gender;

    @TableField("bio")
    private String bio;

    @TableField("expertise")
    private String expertise;

    @TableField("role")
    private String role;

    @TableField("collection_count")
    private Integer collectionCount = 0;

    @TableField("collection_item_count")
    private Integer collectionItemCount = 0;

    @TableField("note_count")
    private Integer noteCount = 0;

    @TableField("unread_notify_count")
    private Integer unreadNotifyCount = 0;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField("token_version")
    private Long tokenVersion;

    @TableField("status")
    private String status;
}

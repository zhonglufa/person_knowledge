package com.qst.lm.dto.collect;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UrlMetadataRequestDTO {
    @NotBlank(message = "链接地址不能为空")
    private String url;
}

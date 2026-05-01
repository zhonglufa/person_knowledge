package com.qst.lm.dto.collect;

import lombok.Data;

@Data
public class UrlMetadataResponseDTO {
    private String url;
    private String normalizedUrl;
    private String title;
    private String description;
    private String source;
    private Integer sourceType;
    private String thumbnail;
}

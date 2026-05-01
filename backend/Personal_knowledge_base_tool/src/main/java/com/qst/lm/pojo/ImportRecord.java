package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入记录实体类
 */
@Data
@TableName("import_record")
public class ImportRecord {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 导入类型:browser_bookmark
     */
    private String importType;

    /**
     * 兼容旧字段名
     */
    public void setSourceType(String sourceType) {
        this.importType = sourceType;
    }

    public String getSourceType() {
        return this.importType;
    }

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 状态:processing/success/failed
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

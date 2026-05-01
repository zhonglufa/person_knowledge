package com.qst.lm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 标注在Controller方法上，用于方法级别的权限校验
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * 权限码
     */
    String value();

    /**
     * 多权限时的逻辑关系
     */
    Logical logical() default Logical.OR;

    enum Logical {
        AND, OR
    }
}

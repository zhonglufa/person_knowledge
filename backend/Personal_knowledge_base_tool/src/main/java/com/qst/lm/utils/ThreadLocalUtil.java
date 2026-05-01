package com.qst.lm.utils;

import java.util.Map;

public final class ThreadLocalUtil {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    private ThreadLocalUtil() {
    }

    public static void set(Map<String, Object> value) {
        THREAD_LOCAL.set(value);
    }

    public static Map<String, Object> get() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}

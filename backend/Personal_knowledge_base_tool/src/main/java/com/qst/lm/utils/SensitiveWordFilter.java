package com.qst.lm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感词过滤工具类
 * 应用启动时从 resources/sensitive_words.txt 加载敏感词列表到内存
 * 提供敏感词检测和过滤功能
 */
@Component
public class SensitiveWordFilter {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordFilter.class);

    /**
     * 使用 ConcurrentHashMap.newKeySet() 创建线程安全的 Set
     * 用于存储敏感词列表
     */
    private static final Set<String> sensitiveWords = ConcurrentHashMap.newKeySet();

    /**
     * 应用启动时加载敏感词列表
     * 从 classpath 下的 sensitive_words.txt 文件读取
     */
    @PostConstruct
    public void init() {
        loadSensitiveWords();
        log.info("敏感词过滤器初始化完成，共加载 {} 个敏感词", sensitiveWords.size());
    }

    /**
     * 从资源文件加载敏感词列表
     */
    private void loadSensitiveWords() {
        String fileName = "sensitive_words.txt";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                log.warn("未找到敏感词文件: {}", fileName);
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    sensitiveWords.add(line);
                }
            }
        } catch (IOException e) {
            log.error("加载敏感词文件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查文本是否包含敏感词
     *
     * @param text 待检查的文本
     * @return 如果包含敏感词返回 true，否则返回 false
     */
    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String word : sensitiveWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将文本中的敏感词替换为 ***
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本，敏感词已被替换为 ***
     */
    public static String filterSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (String word : sensitiveWords) {
            if (result.contains(word)) {
                result = result.replace(word, "***");
            }
        }
        return result;
    }

    /**
     * 获取已加载的敏感词数量
     *
     * @return 敏感词数量
     */
    public static int getSensitiveWordCount() {
        return sensitiveWords.size();
    }

    /**
     * 动态添加敏感词（运行时使用）
     *
     * @param word 要添加的敏感词
     */
    public static void addSensitiveWord(String word) {
        if (word != null && !word.isEmpty()) {
            sensitiveWords.add(word);
        }
    }

    /**
     * 动态移除敏感词（运行时使用）
     *
     * @param word 要移除的敏感词
     */
    public static void removeSensitiveWord(String word) {
        sensitiveWords.remove(word);
    }
}

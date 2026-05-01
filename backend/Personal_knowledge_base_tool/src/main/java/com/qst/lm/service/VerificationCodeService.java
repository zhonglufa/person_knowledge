package com.qst.lm.service;

import com.qst.lm.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 */
@Service
public class VerificationCodeService {
    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeService.class);

    // 验证码有效期5分钟
    private static final long EXPIRE_TIME = TimeUnit.MINUTES.toSeconds(5);

    // 发送频率限制：60秒
    private static final long SEND_INTERVAL = TimeUnit.SECONDS.toSeconds(60);

    // 邮件发送重试次数
    private static final int MAX_RETRY_COUNT = 3;

    // 重试间隔1秒
    private static final long RETRY_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${spring.mail.username}")
    private String username;

    // Redis键前缀
    private static final String CODE_KEY_PREFIX = "verification:code:";
    private static final String TOKEN_KEY_PREFIX = "verification:token:";
    private static final String SEND_TIME_KEY_PREFIX = "verification:send_time:";

    public VerificationCodeService(JavaMailSender mailSender, RedisTemplate<String, Object> redisTemplate) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成6位随机验证码
     */
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 生成唯一验证令牌
     */
    public String generateToken(String email) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, email, EXPIRE_TIME, TimeUnit.SECONDS);
        logger.info("生成验证令牌成功，邮箱: {}, 令牌: {}", email, token);
        return token;
    }

    /**
     * 验证令牌有效性
     */
    public boolean verifyToken(String token) {
        String key = TOKEN_KEY_PREFIX + token;
        Object email = redisTemplate.opsForValue().get(key);

        if (email == null) {
            logger.warn("令牌不存在或已过期，令牌: {}", token);
            return false;
        }

        // 验证成功后删除令牌
        redisTemplate.delete(key);
        logger.info("令牌验证成功，令牌: {}", token);
        return true;
    }

    /**
     * 根据令牌获取邮箱
     */
    public String getEmailByToken(String token) {
        String key = TOKEN_KEY_PREFIX + token;
        Object email = redisTemplate.opsForValue().get(key);
        return email != null ? email.toString() : null;
    }

    /**
     * 发送验证码邮件（支持HTML模板）
     */
    public void sendVerificationCode(String email, String code) {
        int retryCount = 0;
        boolean sent = false;

        while (retryCount < MAX_RETRY_COUNT && !sent) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(email);
                helper.setSubject("【PinBox知识库】注册验证码");
                helper.setFrom(fromEmail, "PinBox知识库");

                // 构建HTML邮件内容
                String htmlContent = buildVerificationEmail(code);
                helper.setText(htmlContent, true);

                mailSender.send(message);

                sent = true;
                logger.info("验证码邮件发送成功，邮箱: {}", email);
            } catch (MessagingException e) {
                retryCount++;
                logger.error("验证码邮件发送失败（尝试{}次），邮箱: {}, 错误: {}", retryCount, email, e.getMessage());

                if (retryCount < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_INTERVAL);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        logger.error("发送邮件重试时线程被中断", ex);
                        break;
                    }
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        if (!sent) {
            logger.error("验证码邮件发送失败（已达最大重试次数），邮箱: {}", email);
            throw new BusinessException("验证码邮件发送失败，请稍后重试");
        }
    }

    /**
     * 构建HTML验证邮件模板
     */
    private String buildVerificationEmail(String code) {
        return "<!DOCTYPE html> " +
               "<html lang=\"zh-CN\"> " +
               "<head> " +
               "    <meta charset=\"UTF-8\"> " +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> " +
               "    <title>PinBox知识库注册验证码</title> " +
               "    <style> " +
               "        body { font-family: 'Microsoft YaHei', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; } " +
               "        .container { max-width: 600px; margin: 30px auto; background-color: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); overflow: hidden; } " +
               "        .header { background-color: #409EFF; color: #fff; padding: 20px; text-align: center; } " +
               "        .header h1 { margin: 0; font-size: 24px; } " +
               "        .content { padding: 30px; } " +
               "        .content p { margin-bottom: 20px; } " +
               "        .code-box { background-color: #f0f9ff; border: 2px solid #e0f2fe; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0; } " +
               "        .code { font-size: 36px; font-weight: bold; color: #409EFF; letter-spacing: 4px; } " +
               "        .note { color: #999; font-size: 14px; margin-top: 10px; } " +
               "        .footer { background-color: #f9f9f9; border-top: 1px solid #eee; padding: 20px; text-align: center; color: #999; font-size: 14px; } " +
               "        .warning { color: #f56c6c; font-weight: bold; } " +
               "    </style> " +
               "</head> " +
               "<body> " +
               "    <div class=\"container\"> " +
               "        <div class=\"header\"> " +
               "            <h1>PinBox知识库注册验证码</h1> " +
               "        </div> " +
               "        <div class=\"content\"> " +
               "            <p>尊敬的用户：</p> " +
               "            <p>感谢您注册PinBox知识库！</p> " +
               "            <div class=\"code-box\"> " +
               "                <div class=\"code\">" + code + "</div> " +
               "                <div class=\"note\">验证码有效期5分钟</div> " +
               "            </div> " +
               "            <p>请在注册页面输入上述验证码完成验证。</p> " +
               "            <p class=\"warning\">重要提示：请勿将验证码泄露给他人，如非本人操作请忽略此邮件。</p> " +
               "        </div> " +
               "        <div class=\"footer\"> " +
               "            <p>此邮件由系统自动发送，请勿回复。</p> " +
               "            <p>&copy; 2024 PinBox知识库</p> " +
               "        </div> " +
               "    </div> " +
               "</body> " +
               "</html>";
    }

    /**
     * 检查是否可以发送验证码（频率限制）
     */
    public boolean canSendCode(String email) {
        String key = SEND_TIME_KEY_PREFIX + email;
        Boolean exists = redisTemplate.hasKey(key);
        return !exists;
    }

    /**
     * 保存验证码
     */
    public void saveCode(String email, String code) {
        String codeKey = CODE_KEY_PREFIX + email;
        String sendTimeKey = SEND_TIME_KEY_PREFIX + email;

        // 保存验证码，设置过期时间
        redisTemplate.opsForValue().set(codeKey, code, EXPIRE_TIME, TimeUnit.SECONDS);

        // 保存发送时间，用于频率限制
        redisTemplate.opsForValue().set(sendTimeKey, String.valueOf(System.currentTimeMillis()), SEND_INTERVAL, TimeUnit.SECONDS);

        logger.info("保存验证码成功，邮箱: {}, 验证码: {}", email, code);
    }

    /**
     * 验证验证码是否有效
     */
    public boolean verifyCode(String email, String code) {
        String key = CODE_KEY_PREFIX + email;
        Object storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            logger.warn("验证码不存在或已过期，邮箱: {}", email);
            return false;
        }

        // 验证验证码是否正确
        boolean isValid = storedCode.equals(code);

        // 验证成功后删除验证码
        if (isValid) {
            redisTemplate.delete(key);
            logger.info("验证码验证成功，邮箱: {}", email);
        } else {
            logger.warn("验证码验证失败，邮箱: {}, 输入验证码: {}, 存储验证码: {}", email, code, storedCode);
        }

        return isValid;
    }

    /**
     * 清除验证码
     * @param email 邮箱地址
     */
    public void clearCode(String email) {
        String codeKey = CODE_KEY_PREFIX + email;
        String sendTimeKey = SEND_TIME_KEY_PREFIX + email;

        // 删除验证码和发送时间记录
        redisTemplate.delete(codeKey);
        redisTemplate.delete(sendTimeKey);

        logger.info("清除验证码成功，邮箱: {}", email);
    }


}

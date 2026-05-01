<template>
  <div class="footer-container">
    <!-- 主要内容区域 -->
    <div class="footer-main">
      <div class="footer-grid">
        <!-- Logo 和描述区域 -->
        <div class="footer-brand-section">
          <div class="brand-wrapper">
            <div class="brand-logo" @click="goToHome">
              <div class="logo-icon">
                <i class="fas fa-book-open"></i>
              </div>
              <div class="brand-text">
                <h2 class="brand-title">知识管理平台</h2>
                <p class="brand-subtitle">Knowledge Management Platform</p>
              </div>
            </div>

            <p class="brand-description">
              专业的知识管理与分享平台，助力个人与团队高效收集、整理和共享知识资源。
            </p>

            <!-- 社交媒体 -->
            <div class="social-section">
              <div class="social-title">关注我们</div>
              <div class="social-icons">
                <el-tooltip
                    content="关注官方微博"
                    placement="top"
                    effect="light"
                >
                  <el-button
                      type="text"
                      class="social-btn weibo-btn"
                      @click="handleSocialClick('微博')"
                  >
                    <i class="fab fa-weibo"></i>
                    <span class="social-text">微博</span>
                  </el-button>
                </el-tooltip>

                <el-tooltip
                    content="微信扫码关注"
                    placement="top"
                    effect="light"
                >
                  <el-button
                      type="text"
                      class="social-btn wechat-btn"
                      @click="handleSocialClick('微信')"
                  >
                    <i class="fab fa-weixin"></i>
                    <span class="social-text">微信</span>
                  </el-button>
                </el-tooltip>

                <el-tooltip
                    content="GitHub开源项目"
                    placement="top"
                    effect="light"
                >
                  <el-button
                      type="text"
                      class="social-btn github-btn"
                      @click="handleSocialClick('GitHub')"
                  >
                    <i class="fab fa-github"></i>
                    <span class="social-text">GitHub</span>
                  </el-button>
                </el-tooltip>

                <el-tooltip
                    content="哔哩哔哩教程"
                    placement="top"
                    effect="light"
                >
                  <el-button
                      type="text"
                      class="social-btn bilibili-btn"
                      @click="handleSocialClick('Bilibili')"
                  >
                    <i class="fab fa-bilibili"></i>
                    <span class="social-text">B站</span>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>

        <!-- 快速链接 -->
        <div class="footer-links-section">
          <div class="links-group">
            <h3 class="links-title">
              <i class="fas fa-link"></i>
              快速链接
            </h3>
            <ul class="links-list">
              <li
                  v-for="link in quickLinks"
                  :key="link.id"
                  class="links-item"
              >
                <el-link
                    :href="link.url"
                    :underline="false"
                    class="link-item"
                    @click="handleLinkClick(link)"
                >
                  <i class="fas fa-chevron-right link-icon"></i>
                  <span class="link-text">{{ link.text }}</span>
                  <span v-if="link.badge" class="link-badge">{{ link.badge }}</span>
                </el-link>
              </li>
            </ul>
          </div>
        </div>

        <!-- 服务支持 -->
        <div class="footer-support-section">
          <div class="support-group">
            <h3 class="support-title">
              <i class="fas fa-life-ring"></i>
              服务支持
            </h3>
            <ul class="support-list">
              <li
                  v-for="support in supports"
                  :key="support.id"
                  class="support-item"
              >
                <el-link
                    :href="support.url"
                    :underline="false"
                    class="support-link"
                    @click="handleSupportClick(support)"
                >
                  <i class="fas fa-circle support-dot"></i>
                  <span class="support-text">{{ support.text }}</span>
                  <span v-if="support.new" class="support-new">NEW</span>
                </el-link>
              </li>
            </ul>
          </div>
        </div>

        <!-- 联系方式 -->
        <div class="footer-contact-section">
          <div class="contact-group">
            <h3 class="contact-title">
              <i class="fas fa-address-book"></i>
              联系我们
            </h3>
            <div class="contact-list">
              <div
                  v-for="contact in contacts"
                  :key="contact.id"
                  class="contact-item"
              >
                <div class="contact-icon-wrapper">
                  <i :class="contact.icon" class="contact-icon"></i>
                </div>
                <div class="contact-details">
                  <div class="contact-type">{{ contact.type }}</div>
                  <div class="contact-value">{{ contact.value }}</div>
                  <div v-if="contact.desc" class="contact-desc">{{ contact.desc }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 版权信息 -->
    <div class="footer-bottom">
      <div class="footer-bottom-content">
        <div class="copyright-section">
          <p class="copyright-text">
            © 2025 知识管理平台 版权所有
          </p>
          <div class="legal-links">
            <el-link
                v-for="link in legalLinks"
                :key="link.id"
                :href="link.url"
                :underline="false"
                class="legal-link"
                @click="handleLegalClick(link)"
            >
              {{ link.text }}
            </el-link>
          </div>
        </div>

      
      </div>
    </div>

    <!-- 回到顶部按钮 -->
    <el-backtop
        target=".footer-container"
        :right="30"
        :bottom="100"
        class="back-to-top"
    >
      <div class="backtop-content">
        <i class="fas fa-arrow-up"></i>
        <span class="backtop-text">顶部</span>
      </div>
    </el-backtop>

  </div>
</template>

<script>
export default {
  name: 'Footer',
  data() {
    return {
      quickLinks: [
        { id: 1, text: '首页', url: '/home', badge: '' },
        { id: 2, text: '关于我们', url: '/about', badge: '' },
        { id: 3, text: '使用指南', url: '/guide', badge: '新' },
        { id: 4, text: '常见问题', url: '/faq', badge: '' },
        { id: 5, text: '知识库', url: '/knowledge', badge: '热' },
        { id: 6, text: '博客', url: '/blog', badge: '' }
      ],
      supports: [
        { id: 1, text: '技术支持', url: '/support', new: true },
        { id: 2, text: '联系我们', url: '/contact', new: false },
        { id: 3, text: '隐私政策', url: '/privacy', new: false },
        { id: 4, text: '用户协议', url: '/terms', new: false },
        { id: 5, text: 'API文档', url: '/api', new: true },
        { id: 6, text: '更新日志', url: '/changelog', new: false }
      ],
      contacts: [
        {
          id: 1,
          type: '电子邮箱',
          value: 'support@example.com',
          icon: 'fas fa-envelope',
          desc: '工作日24小时内回复'
        },
        {
          id: 2,
          type: '客服热线',
          value: '400-123-4567',
          icon: 'fas fa-phone',
          desc: '周一至周五 9:00-18:00'
        },
        {
          id: 3,
          type: '办公地址',
          value: '某某某地址',
          icon: 'fas fa-map-marker-alt',
          desc: ''
        }
      ],
      legalLinks: [
        { id: 1, text: '隐私政策', url: '/privacy' },
        { id: 2, text: '使用条款', url: '/terms' },
        { id: 3, text: '服务协议', url: '/agreement' },
        { id: 4, text: 'Cookie政策', url: '/cookie' }
      ],
      subscribeForm: {
        email: ''
      }
    }
  },
  methods: {
    handleSocialClick(platform) {
      const actions = {
        '微博': () => {
          window.open('https://weibo.com', '_blank');
        },
        '微信': () => {
          this.$message({
            message: '请使用微信扫描二维码关注我们',
            type: 'info',
            duration: 3000,
            customClass: 'wechat-qrcode-message'
          });
        },
        'GitHub': () => {
          window.open('https://github.com', '_blank');
        },
        'Bilibili': () => {
          window.open('https://bilibili.com', '_blank');
        }
      };

      if (actions[platform]) {
        actions[platform]();
      }
    },

    handleLinkClick(link) {
      console.log('点击链接:', link.text);
      this.$router.push(link.url);
    },

    handleSupportClick(support) {
      console.log('点击支持:', support.text);
      this.$router.push(support.url);
    },

    handleLegalClick(link) {
      console.log('点击法律链接:', link.text);
      this.$router.push(link.url);
    },

    handleSubscribe() {
      if (this.subscribeForm.email && this.validateEmail(this.subscribeForm.email)) {
        this.$message.success('订阅成功！');
        this.subscribeForm.email = '';
      } else {
        this.$message.warning('请输入有效的邮箱地址');
      }
    },

    validateEmail(email) {
      const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return re.test(email);
    },

    goToHome() {
      this.$router.push('/home');
    }
  }
}
</script>

<style scoped>
/* 使用全局设计系统变量，Footer特有变量在此声明 */
.footer-container {
  --footer-bg: #1a1a1a;
  --footer-bg-light: #2d2d2d;
  --footer-bg-dark: #0f0f0f;
  --footer-text-primary: #ffffff;
  --footer-text-secondary: #b0b0b0;
  --footer-text-tertiary: #808080;
  --footer-border-color: #333333;
  --footer-border-light: #444444;
  --social-weibo: #e6162d;
  --social-wechat: #07c160;
  --social-github: #333333;
  --social-bilibili: #fb7299;
}

/* 弹性布局容器 */
.footer-container {
  background: linear-gradient(135deg, var(--footer-bg-dark), var(--footer-bg));
  color: var(--footer-text-primary);
  font-family: 'AlibabaPuHuiTi', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 主要区域 */
.footer-main {
  padding: 60px 24px 40px;
  max-width: 1440px;
  margin: 0 auto;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 40px;
  margin-bottom: 40px;
  align-items: start;
}

/* Logo 区域 */
.footer-brand-section {
  grid-column: 1 / -1;
  padding: 0 12px;
}

.brand-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all var(--transition-normal);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.05);
}

.brand-logo:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--primary-color), #667eea);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  flex-shrink: 0;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--footer-text-primary);
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 14px;
  color: var(--footer-text-secondary);
  letter-spacing: 0.5px;
}

.brand-description {
  font-size: 16px;
  line-height: 1.6;
  color: var(--footer-text-secondary);
  max-width: 600px;
}

/* 社交媒体区域 */
.social-section {
}

.social-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--footer-text-primary);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.social-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: var(--primary-color);
  border-radius: 2px;
}

.social-icons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.social-btn {
  margin-left: 0 !important;
  display: flex !important;
  align-items: center !important;
  gap: 8px !important;
  padding: 10px 16px !important;
  border-radius: var(--radius-md) !important;
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid var(--border-light) !important;
  transition: all var(--transition-normal) !important;
  color: var(--footer-text-secondary) !important;
  min-width: 120px;
}

.social-btn:hover {
  transform: translateY(-3px) !important;
  box-shadow: var(--shadow-md) !important;
  color: var(--footer-text-primary) !important;
}

.social-btn i {
  font-size: 18px;
}

.social-text {
  font-size: 14px;
  font-weight: 500;
}

.weibo-btn:hover {
  background: rgba(230, 22, 45, 0.1) !important;
  border-color: var(--social-weibo) !important;
  color: var(--social-weibo) !important;
}

.wechat-btn:hover {
  background: rgba(7, 193, 96, 0.1) !important;
  border-color: var(--social-wechat) !important;
  color: var(--social-wechat) !important;
}

.github-btn:hover {
  background: rgba(51, 51, 51, 0.1) !important;
  border-color: var(--social-github) !important;
  color: var(--footer-text-primary) !important;
}

.bilibili-btn:hover {
  background: rgba(251, 114, 153, 0.1) !important;
  border-color: var(--social-bilibili) !important;
  color: var(--social-bilibili) !important;
}

/* 链接区域 */
.footer-links-section,
.footer-support-section,
.footer-contact-section {
  display: flex;
  flex-direction: column;
  padding: 0 12px;
}

.links-group,
.support-group,
.contact-group {
  flex: 1;
}

.links-title,
.support-title,
.contact-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--footer-text-primary);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--footer-border-color);
}

.links-title i,
.support-title i,
.contact-title i {
  color: var(--primary-color);
  font-size: 18px;
}

/* 链接列表 */
.links-list,
.support-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.links-item,
.support-item {
  margin-bottom: 12px;
}

.link-item,
.support-link {
  display: flex !important;
  align-items: center !important;
  gap: 10px !important;
  padding: 10px 12px !important;
  border-radius: var(--radius-md) !important;
  transition: all var(--transition-normal) !important;
  color: var(--footer-text-secondary) !important;
  position: relative;
  overflow: hidden;
  justify-content: flex-start;
}

.link-item::before,
.support-link::before {
  content: '';
  position: absolute;
  left: -100%;
  top: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: all var(--transition-normal);
}

.link-item:hover::before,
.support-link:hover::before {
  left: 100%;
}

.link-item:hover,
.support-link:hover {
  background: rgba(255, 255, 255, 0.05) !important;
  color: var(--footer-text-primary) !important;
  padding-left: 20px !important;
}

.link-icon {
  font-size: 10px;
  color: var(--primary-color);
  transition: transform var(--transition-fast);
}

.link-item:hover .link-icon {
  transform: translateX(3px);
}

.link-text,
.support-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.link-badge {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  background: var(--primary-color);
  color: white;
  font-weight: 600;
  text-transform: uppercase;
}

.support-dot {
  font-size: 6px;
  color: var(--primary-color);
}

.support-new {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  background: var(--error-color);
  color: white;
  font-weight: 600;
  margin-left: 8px;
}

/* 联系方式 */
.contact-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 32px;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--footer-border-light);
  transition: all var(--transition-normal);
}

.contact-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.contact-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(74, 108, 247, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.contact-icon {
  color: var(--primary-color);
  font-size: 18px;
}

.contact-details {
  flex: 1;
  min-width: 0;
}

.contact-type {
  font-size: 12px;
  color: var(--footer-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.contact-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--footer-text-primary);
  margin-bottom: 2px;
  word-break: break-word;
}

.contact-desc {
  font-size: 12px;
  color: var(--footer-text-tertiary);
}



/* 底部版权信息 */
.footer-bottom {
  border-top: 1px solid var(--footer-border-color);
  padding: 24px;
  background: var(--footer-bg-dark);
}

.footer-bottom-content {
  max-width: 1440px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.copyright-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.copyright-text {
  font-size: 14px;
  color: var(--footer-text-tertiary);
  text-align: center;
}

.legal-links {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 24px;
}

.legal-link {
  font-size: 13px !important;
  color: var(--footer-text-tertiary) !important;
  transition: all var(--transition-normal) !important;
  position: relative;
}

.legal-link:hover {
  color: var(--footer-text-primary) !important;
}

.legal-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--primary-color);
  transition: width var(--transition-normal);
}

.legal-link:hover::after {
  width: 100%;
}



.icp-info,
.security-seal {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--footer-border-light);
}

.icp-icon,
.security-icon {
  color: var(--primary-color);
  font-size: 14px;
}

.icp-text,
.security-text {
  font-size: 12px;
  color: var(--footer-text-tertiary);
}

/* 回到顶部按钮 */
.back-to-top :deep(.el-backtop) {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--primary-color);
  border: 2px solid white;
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-normal);
}

.back-to-top :deep(.el-backtop:hover) {
  background: var(--primary-hover);
  transform: translateY(-3px);
}

.backtop-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: white;
}

.backtop-content i {
  font-size: 18px;
  margin-bottom: 2px;
}

.backtop-text {
  font-size: 10px;
  font-weight: 600;
}

/* 响应式设计 */
@media (min-width: 1024px) {
  .footer-grid {
    grid-template-columns: 1.5fr 1fr 1fr 1.2fr;
    gap: 40px;
  }

  .footer-brand-section {
    grid-column: 1 / 2;
    padding: 0;
  }

  .footer-links-section,
  .footer-support-section,
  .footer-contact-section {
    padding: 0;
  }
}
 
@media (max-width: 1023px) and (min-width: 768px) {
  .footer-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 40px;
  }

  .footer-brand-section {
    grid-column: 1 / -1;
  }

  .social-icons {
    justify-content: flex-start;
  }
}

@media (max-width: 767px) {
  .footer-main {
    padding: 40px 16px 30px;
  }

  .footer-grid {
    gap: 30px;
  }

  .brand-logo {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .logo-icon {
    margin-bottom: 12px;
  }

  .social-icons {
    justify-content: center;
  }

  .social-btn {
    min-width: 100px;
  }

  .contact-item {
    flex-direction: column;
    text-align: center;
    align-items: center;
  }

  .contact-icon-wrapper {
    margin-bottom: 12px;
  }

  .footer-bottom-content {
    gap: 24px;
  }

  .legal-links {
    gap: 16px;
  }

 

  .copyright-text {
    text-align: center;
  }
}

@media (max-width: 480px) {
  .brand-title {
    font-size: 20px;
  }

  .brand-subtitle {
    font-size: 12px;
  }

  .social-btn {
    min-width: 80px;
    padding: 8px 12px !important;
  }

  .social-text {
    font-size: 12px;
  }

  .links-title,
  .support-title,
  .contact-title {
    font-size: 18px;
  }
}

/* 深色模式优化 */
@media (prefers-color-scheme: dark) {
  :root {
    --footer-bg: #121212;
    --footer-bg-light: #1e1e1e;
    --footer-bg-dark: #0a0a0a;
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .social-btn:active,
  .link-item:active,
  .support-link:active,
  .legal-link:active {
    transform: scale(0.95) !important;
    transition: transform 0.1s ease !important;
  }

  .back-to-top :deep(.el-backtop:active) {
    transform: scale(0.95) !important;
  }
}

/* 打印优化 */
@media print {
  .footer-container {
    display: none;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.footer-container > * {
  animation: fadeInUp 0.6s ease-out;
}

/* 性能优化 */
.links-item,
.support-item,
.contact-item,
.social-btn {
  will-change: transform, background-color;
}
</style>

<style>
/* 微信二维码消息样式 */
.wechat-qrcode-message.el-message {
  background: linear-gradient(135deg, #07c160, #1e9b5a);
  border: none;
}

.wechat-qrcode-message .el-message__content {
  color: white;
  font-weight: 500;
}
</style>
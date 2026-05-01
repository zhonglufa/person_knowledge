<template>
  <div class="login-container">
    <!-- 主内容区 -->
    <div class="main-content">
      <div class="login-card">
        <!-- 左侧品牌展示栏 -->
        <div class="brand-section">
          <h1 class="brand-name">智识云</h1>
          <div>
            <h2 class="brand-slogan">构建您的个人知识体系</h2>
            <p class="brand-description">一站式知识管理平台，帮助您收集、整理和拓展知识边界</p>
            <div class="brand-image-container">
              <img src="https://assets.mockplus.cn/ai/newImages/pexels/4397.jpg" alt="知识网络可视化" class="brand-image">
            </div>
          </div>
        </div>

        <!-- 右侧功能表单栏 -->
        <div class="form-section">
          <div class="form-container">
            <!-- 切换面板 -->
            <el-tabs v-model="activeTab" class="form-tabs">
              <el-tab-pane label="登录" name="login">
                <!-- 登录表单 -->
                <el-form
                  :model="loginForm"
                  :rules="loginRules"
                  ref="loginFormRef"
                  class="login-form"
                  @submit.native.prevent="handleLogin"
                >
                  <!-- 账号输入框 -->
                  <el-form-item prop="username">
                    <el-input
                      v-model="loginForm.username"
                      placeholder="账号/邮箱"
                      prefix-icon="el-icon-user"
                      size="large"
                      class="custom-input"
                      @keyup.enter.native="handleLogin"
                      @input="clearFieldError('loginFormRef', 'username')"
                    ></el-input>
                  </el-form-item>

                  <!-- 密码输入框 -->
                  <el-form-item prop="password">
                    <el-input
                      v-model="loginForm.password"
                      :type="showLoginPassword ? 'text' : 'password'"
                      placeholder="密码"
                      prefix-icon="el-icon-lock"
                      size="large"
                      class="custom-input"
                      @keyup.enter.native="handleLogin"
                      @input="clearFieldError('loginFormRef', 'password')"
                    >
                      <template #suffix>
                        <i
                          :class="showLoginPassword ? 'el-icon-view' : 'el-icon-hide'"
                          class="password-toggle"
                          @click="showLoginPassword = !showLoginPassword"
                        ></i>
                      </template>
                    </el-input>
                  </el-form-item>

                  <!-- 记住我和忘记密码 -->
                  <div class="login-options">
                    <el-checkbox v-model="loginForm.rememberMe" class="remember-me">
                      记住我
                    </el-checkbox>
                    <el-link type="primary" :underline="false" class="forgot-password" @click="showForgotDialog = true">
                      忘记密码？
                    </el-link>
                  </div>

                  <!-- 登录按钮 -->
                  <el-button
                    type="primary"
                    size="large"
                    class="submit-btn"
                    :loading="loginLoading"
                    @click="handleLogin"
                  >
                    登录
                  </el-button>

                  <!-- 注册提示 -->
                  <div class="switch-section">
                    <span>还没有账号？</span>
                    <el-link
                      type="primary"
                      :underline="false"
                      class="switch-link"
                      @click="switchToTab('register')"
                    >
                      立即注册
                    </el-link>
                  </div>
                </el-form>
              </el-tab-pane>

              <el-tab-pane label="注册" name="register">
                <!-- 注册表单 -->
                <el-form
                  :model="registerForm"
                  :rules="registerRules"
                  ref="registerFormRef"
                  class="register-form"
                  @submit.native.prevent="handleRegister"
                >
                  <!-- 用户名输入框 -->
                  <el-form-item prop="username">
                    <el-input
                      v-model="registerForm.username"
                      placeholder="用户名"
                      prefix-icon="el-icon-user"
                      size="large"
                      class="custom-input"
                      @input="clearFieldError('registerFormRef', 'username')"
                    ></el-input>
                  </el-form-item>

                  <!-- 邮箱输入框 -->
                  <el-form-item prop="email">
                    <el-input
                      v-model="registerForm.email"
                      placeholder="邮箱"
                      prefix-icon="el-icon-message"
                      size="large"
                      class="custom-input"
                      @input="clearFieldError('registerFormRef', 'email')"
                    ></el-input>
                  </el-form-item>

                  <!-- 验证码输入框 -->
                  <el-form-item prop="verifyCode">
                    <el-input
                      v-model="registerForm.verifyCode"
                      placeholder="验证码"
                      prefix-icon="el-icon-key"
                      size="large"
                      class="custom-input"
                      maxlength="6"
                      @input="clearFieldError('registerFormRef', 'verifyCode')"
                    >
                      <template #suffix>
                        <el-button
                          type="text"
                          :disabled="countdown > 0 || sendCodeLoading"
                          :loading="sendCodeLoading"
                          @click="sendVerifyCodeHandler"
                          class="send-code-btn"
                        >
                          {{ countdown > 0 ? `${countdown}秒后重新获取` : '获取验证码' }}
                        </el-button>
                      </template>
                    </el-input>
                  </el-form-item>

                  <!-- 密码输入框 -->
                  <el-form-item prop="password">
                    <el-input
                      v-model="registerForm.password"
                      :type="showRegisterPassword ? 'text' : 'password'"
                      placeholder="密码"
                      prefix-icon="el-icon-lock"
                      size="large"
                      class="custom-input"
                      @input="clearFieldError('registerFormRef', 'password')"
                    >
                      <template #suffix>
                        <i
                          :class="showRegisterPassword ? 'el-icon-view' : 'el-icon-hide'"
                          class="password-toggle"
                          @click="showRegisterPassword = !showRegisterPassword"
                        ></i>
                      </template>
                    </el-input>
                  </el-form-item>

                  <!-- 用户协议 -->
                  <el-form-item prop="agreement">
                    <el-checkbox v-model="registerForm.agreement" class="agreement-checkbox">
                      我已阅读并同意
                      <el-link type="primary" :underline="false" class="agreement-link">《用户协议》</el-link>
                      和
                      <el-link type="primary" :underline="false" class="agreement-link">《隐私政策》</el-link>
                    </el-checkbox>
                  </el-form-item>

                  <!-- 注册按钮 -->
                  <el-button
                    type="primary"
                    size="large"
                    class="submit-btn"
                    :loading="registerLoading"
                    @click="handleRegister"
                  >
                    注册
                  </el-button>

                  <!-- 登录提示 -->
                  <div class="switch-section">
                    <span>已有账号？</span>
                    <el-link
                      type="primary"
                      :underline="false"
                      class="switch-link"
                      @click="switchToTab('login')"
                    >
                      立即登录
                    </el-link>
                  </div>
                </el-form>
              </el-tab-pane>
            </el-tabs>

            <!-- 忘记密码对话框 -->
            <el-dialog
              title="忘记密码"
              :visible.sync="showForgotDialog"
              width="420px"
              :close-on-click-modal="false"
              @closed="resetForgotForm"
            >
              <el-form
                :model="forgotForm"
                :rules="forgotRules"
                ref="forgotFormRef"
                label-width="0"
              >
                <el-form-item prop="email">
                  <el-input
                    v-model="forgotForm.email"
                    placeholder="请输入注册邮箱"
                    prefix-icon="el-icon-message"
                    size="large"
                    class="custom-input"
                  ></el-input>
                </el-form-item>

                <el-form-item prop="code">
                  <el-input
                    v-model="forgotForm.code"
                    placeholder="验证码"
                    prefix-icon="el-icon-key"
                    size="large"
                    class="custom-input"
                    maxlength="6"
                  >
                    <template #suffix>
                      <el-button
                        type="text"
                        :disabled="forgotCountdown > 0 || forgotSendLoading"
                        :loading="forgotSendLoading"
                        @click="sendForgotCode"
                        class="send-code-btn"
                      >
                        {{ forgotCountdown > 0 ? `${forgotCountdown}秒后重新获取` : '获取验证码' }}
                      </el-button>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item prop="newPassword">
                  <el-input
                    v-model="forgotForm.newPassword"
                    :type="showForgotPassword ? 'text' : 'password'"
                    placeholder="新密码"
                    prefix-icon="el-icon-lock"
                    size="large"
                    class="custom-input"
                  >
                    <template #suffix>
                      <i
                        :class="showForgotPassword ? 'el-icon-view' : 'el-icon-hide'"
                        class="password-toggle"
                        @click="showForgotPassword = !showForgotPassword"
                      ></i>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item prop="confirmPassword">
                  <el-input
                    v-model="forgotForm.confirmPassword"
                    :type="showForgotConfirm ? 'text' : 'password'"
                    placeholder="确认新密码"
                    prefix-icon="el-icon-lock"
                    size="large"
                    class="custom-input"
                  >
                    <template #suffix>
                      <i
                        :class="showForgotConfirm ? 'el-icon-view' : 'el-icon-hide'"
                        class="password-toggle"
                        @click="showForgotConfirm = !showForgotConfirm"
                      ></i>
                    </template>
                  </el-input>
                </el-form-item>
              </el-form>

              <div slot="footer">
                <el-button @click="showForgotDialog = false">取 消</el-button>
                <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">重置密码</el-button>
              </div>
            </el-dialog>

            <!-- 第三方登录区 -->
            <div class="social-login">
              <div class="divider">
                <div class="divider-line"></div>
                <span class="divider-text">其他登录方式</span>
                <div class="divider-line"></div>
              </div>

              <div class="social-buttons">
                <el-button
                  circle
                  class="social-btn wechat-btn"
                  @click="handleWechatLogin"
                >
                  <i class="fab fa-weixin"></i>
                </el-button>
                <el-button
                  circle
                  class="social-btn qq-btn"
                  @click="handleQQLogin"
                >
                  <i class="fab fa-qq"></i>
                </el-button>
                <el-button
                  circle
                  class="social-btn weibo-btn"
                  @click="handleWeiboLogin"
                >
                  <i class="fab fa-weibo"></i>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

// 导入 mapState, mapGetters, mapActions 来使用 Vuex
import { mapState, mapGetters, mapActions } from 'vuex';
import { resetPassword } from '@/api/user';
import { sendVerifyCode as sendCode } from '@/api/auth';

export default {
  name: 'LoginPage',
  data() {
    // 密码验证规则 - 增强复杂度
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'));
      } else if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'));
      } else if (value.length > 20) {
        callback(new Error('密码长度不能超过20位'));
      } else if (!/[a-z]/.test(value) || !/[A-Z]/.test(value)) {
        callback(new Error('密码必须包含大小写字母'));
      } else if (!/[0-9]/.test(value)) {
        callback(new Error('密码必须包含数字'));
      } else {
        callback();
      }
    };

    // 验证码验证规则
    const validateVerifyCode = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入验证码'));
      } else if (value.length !== 6) {
        callback(new Error('验证码必须为6位数字'));
      } else if (!/^\d+$/.test(value)) {
        callback(new Error('验证码只能包含数字'));
      } else {
        callback();
      }
    };

    // 邮箱验证规则 - 增强验证
    const validateEmail = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入邮箱地址'));
      } else {
        const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailReg.test(value)) {
          callback(new Error('请输入正确的邮箱地址格式'));
        } else {
          callback();
        }
      }
    };


    return {
      // 当前激活的选项卡
      activeTab: 'login',

      // 密码显示状态
      showLoginPassword: false,
      showRegisterPassword: false,

      // 验证码相关状态
      countdown: 0,
      sendCodeLoading: false,

      // 忘记密码相关状态
      showForgotDialog: false,
      showForgotPassword: false,
      showForgotConfirm: false,
      forgotCountdown: 0,
      forgotSendLoading: false,
      resetLoading: false,
      forgotForm: {
        email: '',
        code: '',
        newPassword: '',
        confirmPassword: ''
      },

      // 登录表单数据
      loginForm: {
        username: '',
        password: '',
        rememberMe: false
      },

      // 登录表单验证规则
      loginRules: {
        username: [
          { required: true, message: '请输入账号或邮箱', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { validator: validatePassword, message: '密码必须包含大小写字母和数字，长度6-20位', trigger: 'blur' }
        ]
      },

      // 注册表单数据
      registerForm: {
        username: '',
        email: '',
        password: '',
        verifyCode: '',
        agreement: false
      },

      // 注册表单验证规则
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到20 个字符', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { validator: validateEmail, message: '请输入正确的邮箱地址格式', trigger: 'blur' }
        ],
        verifyCode: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { validator: validateVerifyCode, message: '验证码必须为6位数字', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { validator: validatePassword, message: '密码必须包含大小写字母和数字，长度6-20位', trigger: 'blur' }
        ],
        agreement: [
          { required: true, message: '请阅读并同意用户协议', trigger: 'change' },
          { validator: (rule, value, callback) => {
              value ? callback() : callback(new Error('必须同意用户协议才能继续注册'));
            }, trigger: 'change' }
        ]
      }
    };
  },

  // 映射 Vuex 状态和 getter
  computed: {
    ...mapState('user', ['loading', 'error']),
    ...mapGetters('user', ['isLoading', 'getError', 'isAuthenticated']),
    // 忘记密码表单验证规则（需要引用this.forgotForm，故放在computed中）
    forgotRules() {
      const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      const vm = this;
      return {
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { pattern: emailReg, message: '请输入正确的邮箱地址格式', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码必须为6位', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          {
            validator: function(rule, value, callback) {
              if (value !== vm.forgotForm.newPassword) {
                callback(new Error('两次输入的密码不一致'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ]
      };
    },
    // 计算登录加载状态
    loginLoading() {
      return this.isLoading && this.activeTab === 'login';
    },
    // 计算注册加载状态
    registerLoading() {
      return this.isLoading && this.activeTab === 'register';
    }
  },

  // 监听 activeTab 变化，自动清除校验
  watch: {
    activeTab(newTab, oldTab) {
      // 当Tab切换时，清除旧表单的校验
      if (oldTab === 'login' && this.$refs.loginFormRef) {
        this.$refs.loginFormRef.clearValidate();
      } else if (oldTab === 'register' && this.$refs.registerFormRef) {
        this.$refs.registerFormRef.clearValidate();
      }
    }
  },

  // 映射 Vuex actions
  methods: {
    ...mapActions('user', ['login', 'register', 'sendVerifyCode']),

    /**
     * 切换表单Tab（封装切换+清除校验逻辑）
     * @param {string} tab - 'login' 或 'register'
     */
    switchToTab(tab) {
      // 1. 清除当前表单的校验
      if (this.activeTab === 'login' && this.$refs.loginFormRef) {
        this.$refs.loginFormRef.clearValidate();
      } else if (this.activeTab === 'register' && this.$refs.registerFormRef) {
        this.$refs.registerFormRef.clearValidate();
      }
      
      // 2. 切换Tab
      this.activeTab = tab;
      
      // 3. 下一个tick清除新表单的校验（避免显示旧状态）
      this.$nextTick(() => {
        if (tab === 'login' && this.$refs.loginFormRef) {
          this.$refs.loginFormRef.clearValidate();
        } else if (tab === 'register' && this.$refs.registerFormRef) {
          this.$refs.registerFormRef.clearValidate();
        }
      });
    },

    /**
     * 实时清除指定字段的校验错误（输入时调用）
     * @param {string} formRef - 表单ref名称
     * @param {string} field - 字段名称
     */
    clearFieldError(formRef, field) {
      this.$nextTick(() => {
        const form = this.$refs[formRef];
        if (form && form.clearValidate) {
          form.clearValidate(field);
        }
      });
    },

    /**
     * 处理登录
     */
    async handleLogin() {
      this.$refs.loginFormRef.validate(async (valid) => {
        if (valid) {
          try {
            // 调用store中的登录action
            const user = await this.login({
              username: this.loginForm.username,
              password: this.loginForm.password,
              rememberMe: this.loginForm.rememberMe
            });

            // 登录成功处理
            this.$message({
              message: '登录成功！',
              type: 'success',
              duration: 2000
            });

            // 保存记住我状态
            if (this.loginForm.rememberMe) {
              localStorage.setItem('rememberMe', 'true');
              localStorage.setItem('username', this.loginForm.username);
            } else {
              localStorage.removeItem('rememberMe');
              localStorage.removeItem('username');
            }

            // 登录成功后跳转到个人中心
            const redirect = this.$route.query.redirect || '/personal/center';
            this.$router.push(redirect);


          } catch (error) {
            // 处理登录失败 - 错误已经在Vuex action 中处理
            this.$message({
              message: error.message,
              type: 'error',
              duration: 3000
            });
          }
        } else {
          this.$message({
            message: '请填写正确的登录信息',
            type: 'warning',
            duration: 2000
          });
          return false;
        }
      });
    },

    /**
     * 发送验证码处理函数
     */
    async sendVerifyCodeHandler() {
      // 检查邮箱是否为空
      if (!this.registerForm.email) {
        this.$message.error('请先输入邮箱地址');
        return;
      }

      // 使用我们定义的邮箱验证规则检查邮箱格式
      const validateEmail = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请输入邮箱地址'));
        } else {
          const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
          if (!emailReg.test(value)) {
            callback(new Error('请输入正确的邮箱地址格式'));
          } else {
            callback();
          }
        }
      };

      try {
        await new Promise((resolve, reject) => {
          validateEmail({}, this.registerForm.email, (error) => {
            if (error) {
              reject(error);
            } else {
              resolve();
            }
          });
        });
      } catch (error) {
        this.$message.error(error.message);
        return;
      }

      // 检查是否正在发送或倒计时中
      if (this.sendCodeLoading || this.countdown > 0) {
        return;
      }

      this.sendCodeLoading = true;

      try {
        // 调用发送验证码API - 已经映射到Vuex action
        const response = await this.sendVerifyCode({ email: this.registerForm.email });

        this.sendCodeLoading = false;
        this.countdown = 60; // 60秒倒计时
        // 开始倒计时
        const timer = setInterval(() => {
          if (this.countdown > 0) {
            this.countdown--;
          } else {
            clearInterval(timer);
          }
        }, 1000);

        // 提供更详细的成功提示
        this.$message({
          message: '验证码已发送到您的邮箱，请注意查收（有效期10分钟）',
          type: 'success',
          duration: 5000
        });
      } catch (error) {
        this.sendCodeLoading = false;
        let errorMessage = '发送验证码失败，请稍后重试';

        // 根据不同错误类型提供更具体的提示
        if (error.response) {
          if (error.response.status === 429) {
            errorMessage = '发送频率过高，请稍后再试';
          } else if (error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
          } else if (error.response.status === 400) {
            errorMessage = '邮箱格式不正确，请检查后重试';
          }
        } else if (error.message) {
          errorMessage = error.message;
        }

        this.$message({
          message: errorMessage,
          type: 'error',
          duration: 3000
        });
      }
    },

    /**
     * 处理注册
     */
    async handleRegister() {
      this.$refs.registerFormRef.validate(async (valid) => {
        if (valid) {
          try {
            // 调用store中的注册action
            const result = await this.register({
              username: this.registerForm.username,
              email: this.registerForm.email,
              password: this.registerForm.password,
              code: this.registerForm.verifyCode
            });

            // 注册成功处理
            const successMessage = result?.message || '注册成功！';
            this.$message({
              message: successMessage,
              type: 'success',
              duration: 2000
            });

            // 切换到登录标签（使用switchToTab清除校验）
            this.switchToTab('login');

            // 清空注册表单
            this.$refs.registerFormRef.resetFields();

            // 自动填充用户名到登录表单
            this.loginForm.username = this.registerForm.username;

          } catch (error) {
            // 处理注册失败 - 错误已经在Vuex action 中处理
            this.$message({
              message: error.message,
              type: 'error',
              duration: 3000
            });
          }
        } else {
          this.$message({
            message: '请填写正确的注册信息',
            type: 'warning',
            duration: 2000
          });
          return false;
        }
      });
    },

    /**
     * 发送忘记密码验证码
     */
    async sendForgotCode() {
      if (!this.forgotForm.email) {
        this.$message.error('请先输入邮箱地址');
        return;
      }
      const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!emailReg.test(this.forgotForm.email)) {
        this.$message.error('请输入正确的邮箱地址格式');
        return;
      }
      if (this.forgotSendLoading || this.forgotCountdown > 0) return;

      this.forgotSendLoading = true;
      try {
        await sendCode({ email: this.forgotForm.email });
        this.forgotSendLoading = false;
        this.forgotCountdown = 60;
        const timer = setInterval(() => {
          if (this.forgotCountdown > 0) {
            this.forgotCountdown--;
          } else {
            clearInterval(timer);
          }
        }, 1000);
        this.$message.success('验证码已发送，请注意查收');
      } catch (error) {
        this.forgotSendLoading = false;
        const msg = (error.response && error.response.data && error.response.data.message) || error.message || '发送验证码失败';
        this.$message.error(msg);
      }
    },

    /**
     * 处理重置密码
     */
    async handleResetPassword() {
      this.$refs.forgotFormRef.validate(async (valid) => {
        if (!valid) return;
        this.resetLoading = true;
        try {
          await resetPassword({
            email: this.forgotForm.email,
            code: this.forgotForm.code,
            newPassword: this.forgotForm.newPassword
          });
          this.$store.commit('user/CLEAR_USER_STATE')
          this.$message.success('密码重置成功，请使用新密码重新登录');
          this.showForgotDialog = false;
          this.activeTab = 'login';
        } catch (error) {
          const msg = (error.response && error.response.data && error.response.data.message) || error.message || '重置密码失败';
          this.$message.error(msg);
        } finally {
          this.resetLoading = false;
        }
      });
    },

    /**
     * 重置忘记密码表单
     */
    resetForgotForm() {
      this.forgotForm = { email: '', code: '', newPassword: '', confirmPassword: '' };
      this.showForgotPassword = false;
      this.showForgotConfirm = false;
      if (this.$refs.forgotFormRef) {
        this.$refs.forgotFormRef.resetFields();
      }
    },

    /**
     * 微信登录
     */
    handleWechatLogin() {
      this.$message({
        message: '正在跳转到微信登录...',
        type: 'info',
        duration: 2000
      });
    },

    /**
     * QQ登录
     */
    handleQQLogin() {
      this.$message({
        message: '正在跳转到QQ登录...',
        type: 'info',
        duration: 2000
      });
    },

    /**
     * 微博登录
     */
    handleWeiboLogin() {
      this.$message({
        message: '正在跳转到微博登录...',
        type: 'info',
        duration: 2000
      });
    }
  },
  mounted() {
    // 组件挂载后可以执行的初始化操作
    console.log('智识云登录注册页面已加载');

    // 检查是否记住我
    const rememberMe = localStorage.getItem('rememberMe');
    if (rememberMe === 'true') {
      this.loginForm.rememberMe = true;
      // 如果记住我，自动填充用户名
      const username = localStorage.getItem('username');
      if (username) {
        this.loginForm.username = username;
      }
    }

    // 检查URL参数，自动切换到注册表单
    const tab = this.$route.query.tab;
    if (tab === 'register') {
      // 使用$nextTick确保DOM已渲染
      this.$nextTick(() => {
        this.switchToTab('register');
      });
    }
  }
};
</script>

<style scoped>
/* 登录页面主容器 */
.login-container {
  min-height: 100vh;
  background-color: var(--bg-canvas);
  display: flex;
  flex-direction: column;
  font-family: var(--font-family-base);
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-5);
  min-height: calc(100vh - var(--space-10));
}

/* 登录卡片容器 */
.login-card {
  width: 100%;
  max-width: 1200px;
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 在大屏幕上实现左右分栏布局 */
@media (min-width: 768px) {
  .login-card {
    flex-direction: row;
    max-height: 100vh;
    overflow: hidden;
  }

  .brand-section {
    flex: 1;
    height: auto;
    max-height: 90vh;
    min-height: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    overflow-y: hidden;
  }

  .form-section {
    flex: 1;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }
}

/* 品牌展示区 */
.brand-section {
  width: 100%;
  padding: var(--space-6) var(--space-8);
  background: linear-gradient(135deg, rgba(74, 108, 247, 0.05) 0%, rgba(255, 255, 255, 1) 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  box-sizing: border-box;
  max-height: 90vh;
}

/* 品牌logo */
.brand-logo {
  width: 60px;
  height: 60px;
  background-color: var(--primary-color);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-3);
}

.brand-logo i {
  color: white;
  font-size: 24px;
}

/* 品牌名称 */
.brand-name {
  font-size: clamp(20px, 3vw, 28px);
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: var(--space-3);
}

/* 品牌标语 */
.brand-slogan {
  font-size: clamp(18px, 2.5vw, 24px);
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: var(--space-3);
  line-height: 1.3;
}

/* 品牌描述 */
.brand-description {
  font-size: clamp(14px, 1.5vw, 16px);
  color: var(--text-regular);
  margin-bottom: var(--space-5);
  max-width: 380px;
  line-height: 1.4;
}

/* 品牌图片 */
.brand-image-container {
  position: relative;
  margin-top: var(--space-3);
}

.brand-image {
  width: 100%;
  max-width: 280px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
}

.brand-image-tag {
  position: absolute;
  bottom: -8px;
  right: -8px;
  background-color: var(--bg-container);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-base);
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-regular);
}

.brand-image-tag i {
  color: var(--primary-color);
}

/* 表单区域 */
.form-section {
  width: 100%;
  padding: var(--space-10) var(--space-8);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.form-container {
  width: 100%;
  max-width: 400px;
  background-color: var(--bg-container);
  border-radius: var(--radius-md);
  padding: 0;
  min-height: 580px;
  display: flex;
  flex-direction: column;
}

/* 表单标签页 */
.form-tabs {
  margin-bottom: var(--space-6);
  width: 100%;
}

/* 标签页样式 */
::v-deep .el-tabs__nav-wrap::after {
  height: 1px;
  background-color: var(--border-dark);
}

::v-deep .el-tabs__active-bar {
  background-color: var(--primary-color);
  height: 2px;
}

::v-deep .el-tabs__item {
  color: var(--text-regular);
  font-weight: 500;
  font-family: var(--font-family-base);
  font-size: 16px;
  padding: 0 var(--space-5);
  height: 48px;
  line-height: 48px;
  transition: color var(--transition-fast);
}

::v-deep .el-tabs__item.is-active {
  color: var(--primary-color);
}

::v-deep .el-tabs__item:hover {
  color: var(--primary-color);
}

/* 登录表单 */
.login-form,
.register-form {
  width: 100%;
  min-height: 420px;
  display: flex;
  flex-direction: column;
}

/* 表单项间距 */
.login-form .el-form-item,
.register-form .el-form-item {
  margin-bottom: var(--space-5);
}

/* 自定义输入框 */
.custom-input ::v-deep .el-input__inner {
  border-radius: 20px;
  border: 1px solid var(--border-dark);
  height: 48px;
  padding-left: 42px;
  font-family: var(--font-family-base);
  font-size: 15px;
  transition: var(--transition-fast);
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.04);
}

/* 发送验证码按钮 */
.send-code-btn {
  padding: 0 var(--space-3);
  font-size: 14px;
  color: var(--primary-color);
  font-weight: 500;
  transition: color var(--transition-fast);
}

.send-code-btn:disabled {
  color: var(--text-disabled);
}

.custom-input ::v-deep .el-input__inner:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-bg);
  outline: none;
}

.custom-input ::v-deep .el-input__prefix {
  left: 14px;
  color: var(--text-secondary);
  font-size: 16px;
}

/* 密码显示/隐藏按钮 */
.password-toggle {
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 16px;
  transition: color var(--transition-fast);
  padding: var(--space-1);
}

.password-toggle:hover {
  color: var(--text-regular);
}

/* 记住我与忘记密码 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

/* 记住我复选框 */
.remember-me ::v-deep .el-checkbox__label {
  color: var(--text-regular);
  font-size: 14px;
  font-family: var(--font-family-base);
}

::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

/* 忘记密码链接 */
.forgot-password {
  font-size: 14px;
  color: var(--primary-color);
  font-weight: 500;
  transition: color var(--transition-fast);
}

.forgot-password:hover {
  color: var(--primary-hover);
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 600;
  font-family: var(--font-family-base);
  background-color: var(--primary-color);
  border: none;
  transition: var(--transition-fast);
  margin-bottom: var(--space-5);
  position: relative;
}

.submit-btn:hover:not(:disabled) {
  background-color: var(--primary-hover);
  box-shadow: var(--shadow-primary);
  transform: translateY(-1px);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}

.submit-btn:disabled,
.submit-btn.is-loading {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 切换链接区域 */
.switch-section {
  text-align: center;
  font-size: 14px;
  color: var(--text-regular);
}

.switch-link {
  font-size: 14px;
  color: var(--primary-color);
  font-weight: 500;
  margin-left: var(--space-1);
  transition: color var(--transition-fast);
}

.switch-link:hover {
  color: var(--primary-hover);
}

/* 用户协议 */
.agreement-checkbox ::v-deep .el-checkbox__label {
  color: var(--text-regular);
  font-size: 13px;
  font-family: var(--font-family-base);
  line-height: 1.4;
}

.agreement-link {
  font-size: 13px;
  color: var(--primary-color);
  font-weight: 500;
  transition: color var(--transition-fast);
}

.agreement-link:hover {
  color: var(--primary-hover);
}

/* 第三方登录区 */
.social-login {
  width: 100%;
  text-align: center;
}

/* 分割线 */
.divider {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-6);
}

.divider-line {
  flex: 1;
  height: 1px;
  background-color: var(--border-light);
}

.divider-text {
  padding: 0 var(--space-4);
  font-size: 13px;
  color: var(--text-secondary);
}

/* 第三方登录按钮 */
.social-buttons {
  display: flex;
  justify-content: center;
  gap: var(--space-5);
}

.social-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  font-size: 20px;
  transition: var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-base);
}

.social-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.social-btn:active {
  transform: translateY(0);
}

/* 微信登录 */
.wechat-btn {
  background-color: #07C160;
  color: white;
}

.wechat-btn:hover {
  background-color: #06b056;
}

/* QQ登录 */
.qq-btn {
  background-color: #12B7F5;
  color: white;
}

.qq-btn:hover {
  background-color: #0ea8e0;
}

/* 微博登录 */
.weibo-btn {
  background-color: #E6162D;
  color: white;
}

.weibo-btn:hover {
  background-color: #d3142a;
}

/* Tab 面板切换过渡 */
::v-deep .el-tabs__content {
  transition: var(--transition-normal);
}

::v-deep .el-tab-pane {
  animation: fadeSlideIn 0.25s ease;
}

@keyframes fadeSlideIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 忘记密码对话框增强 */
::v-deep .el-dialog {
  border-radius: var(--radius-lg);
}

::v-deep .el-dialog__header {
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-light);
}

::v-deep .el-dialog__body {
  padding: var(--space-6);
}

::v-deep .el-dialog__footer {
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--border-light);
}

/* 响应式设计 - 中等屏幕及以上 */
@media (min-width: 768px) {
  .login-card {
    flex-direction: row;
    min-height: 600px;
  }

  .brand-section {
    width: 50%;
    padding: var(--space-16) var(--space-12);
    align-items: flex-start;
    text-align: left;
  }

  .form-section {
    width: 50%;
    padding: var(--space-16) var(--space-10);
  }

  .brand-slogan {
    font-size: 32px;
    margin-bottom: var(--space-5);
  }

  .brand-description {
    font-size: 18px;
    margin-bottom: var(--space-8);
  }

  .brand-image-tag {
    display: flex;
  }
}

/* 响应式设计 - 小屏幕 */
@media (max-width: 767px) {
  .brand-image-tag {
    display: none;
  }

  .login-card {
    border-radius: var(--radius-md);
  }

  .social-buttons {
    gap: var(--space-4);
  }

  .social-btn {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
}

/* 响应式设计 - 超小屏幕 */
@media (max-width: 575px) {
  .main-content {
    padding: var(--space-3);
  }

  .brand-section {
    padding: var(--space-8) var(--space-5);
  }

  .form-section {
    padding: var(--space-8) var(--space-5);
  }

  .brand-slogan {
    font-size: 20px;
  }

  .brand-description {
    font-size: 14px;
  }

  .submit-btn {
    height: 44px;
    font-size: 15px;
  }

  .custom-input ::v-deep .el-input__inner {
    height: 44px;
    font-size: 14px;
  }
}
</style>

<style>
/*!* 全局样式 *!*/
/*@import url('https://assets.mockplus.cn/ai/AlibabaPuHuiTi2.0.css');*/



</style>

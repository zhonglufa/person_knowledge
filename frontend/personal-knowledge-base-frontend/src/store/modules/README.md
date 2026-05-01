# User 模块详细说明

## 概述

User 模块负责管理用户认证相关的所有状态和业务逻辑。

## 状态 (State)

| 状态 | 类型 | 描述 |
|------|------|------|
| userInfo | Object/null | 用户基本信息 |
| token | String/null | JWT 认证令牌（自动持久化） |
| loading | Boolean | API 请求加载状态 |
| error | String/null | 错误信息 |
| isAuthenticated | Boolean | 用户认证状态 |

## 计算属性 (Getters)

| Getter | 返回值 | 描述 |
|--------|--------|------|
| getUserInfo | Object/null | 获取用户信息 |
| isAuthenticated | Boolean | 检查是否已认证 |
| isLoading | Boolean | 获取加载状态 |
| getError | String/null | 获取错误信息 |
| getUserRole | String/null | 获取用户角色 |

## 状态变更 (Mutations)

| Mutation | 参数 | 描述 |
|----------|------|------|
| SET_USER_INFO | userInfo | 设置用户信息 |
| SET_TOKEN | token | 设置认证令牌（自动持久化） |
| SET_LOADING | loading | 设置加载状态 |
| SET_ERROR | error | 设置错误信息 |
| SET_AUTHENTICATED | isAuthenticated | 设置认证状态 |
| CLEAR_USER_STATE | - | 清除用户状态 |

## 业务操作 (Actions)

### login(credentials)
- **参数**: `{ username, password, rememberMe }`
- **返回**: 用户信息对象
- **描述**: 用户登录，获取令牌并更新状态

### register(userData)
- **参数**: `{ username, email, password, code }`
- **返回**: 用户信息对象
- **描述**: 用户注册，获取令牌并更新状态

### logout()
- **参数**: 无
- **返回**: Promise
- **描述**: 用户登出，清除本地状态

### fetchUserInfo()
- **参数**: 无
- **返回**: 用户信息对象
- **描述**: 获取当前用户信息

### sendVerifyCode(emailData)
- **参数**: `{ email }`
- **返回**: Promise
- **描述**: 发送验证码到指定邮箱

## 使用示例

### 在组件中使用
```javascript
import { mapState, mapGetters, mapActions } from 'vuex';

export default {
  computed: {
    ...mapState('user', ['userInfo', 'token', 'loading', 'error']),
    ...mapGetters('user', ['isAuthenticated', 'getUserRole'])
  },
  
  methods: {
    ...mapActions('user', ['login', 'logout', 'fetchUserInfo'])
  }
}
```

### 直接调用
```javascript
// 登录
try {
  const user = await this.$store.dispatch('user/login', credentials);
  console.log('登录成功', user);
} catch (error) {
  console.error('登录失败', error.message);
}

// 登出
await this.$store.dispatch('user/logout');
```

# Home 模块

## 概述

Home 模块负责管理首页（GuestHome.vue）的相关状态，包括热门知识、推荐收藏集等数据。

## 模块结构

```
src/
└── store/
    └── modules/
        └── home.js    # 首页状态管理模块
```

## 状态 (State)

- `popularKnowledge`: 热门知识列表
- `recommendedCollections`: 推荐收藏集列表
- `loading`: 加载状态
- `loadError`: 错误状态
- `errorMessage`: 错误信息

## Getter

- `getPopularKnowledge`: 获取热门知识
- `getRecommendedCollections`: 获取推荐收藏集
- `isLoading`: 获取加载状态
- `hasLoadError`: 获取错误状态

## Mutation

- `SET_POPULAR_KNOWLEDGE`: 设置热门知识
- `SET_RECOMMENDED_COLLECTIONS`: 设置推荐收藏集
- `SET_LOADING`: 设置加载状态
- `SET_LOAD_ERROR`: 设置错误状态
- `SET_ERROR_MESSAGE`: 设置错误信息
- `CLEAR_ERROR`: 清除错误状态

## Action

- `fetchHomeData`: 获取首页所有数据
- `fetchPopularKnowledge`: 获取热门知识
- `fetchRecommendedCollections`: 获取推荐收藏集

## 使用方法

在组件中使用：

```javascript
import { mapState, mapActions } from 'vuex';

export default {
  computed: {
    ...mapState('home', ['popularKnowledge', 'recommendedCollections', 'loading'])
  },
  methods: {
    ...mapActions('home', ['fetchHomeData', 'fetchPopularKnowledge'])
  }
}

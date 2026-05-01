import Vue from 'vue';
import Vuex from 'vuex';
import user from './modules/user';
import home from './modules/home';
import collect from './modules/collect';
import interaction from './modules/interaction';
import settings from './modules/settings';
import permission from './modules/permission';

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    user,
    home,
    collect,
    interaction,
    settings,
    permission
  },
  // 在开发环境（写代码环境）下启用严格模式——（纠错助手）  还有 Testing（测试环境）、Staging（预发布环境）、Production（生产环境）
  strict: process.env.NODE_ENV === 'development'
});
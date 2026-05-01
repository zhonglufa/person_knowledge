<template>
  <!-- 直接使用Header组件 -->
  <Header 
    v-bind="$props"
    v-on="$listeners"
  />
</template>

<script>
import Header from './Header.vue'

export default {
  name: 'TopNavbar',
  components: {
    Header
  },
  // 传递所有props给Header组件
  props: Header.props,
  // 传递所有事件监听器给Header组件
  inheritAttrs: false
}
</script>

<style scoped>
/* 样式继承自Header组件 */
</style>

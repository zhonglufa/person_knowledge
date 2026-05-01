import { defineConfig } from 'vite'
import vue2 from '@vitejs/plugin-vue2'  // Vue 2

import { resolve } from 'path'
import { fileURLToPath } from 'url'

const __dirname = fileURLToPath(new URL('.', import.meta.url)) // 获取当前文件所在目录的绝对路径

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue2()],
  server: {
    port: 5000,  // 指定端口号
    host: 'localhost' // 可选，允许外部访问
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src') // 这里定义了 @ 别名
    },
  },
  // 添加编码配置
  build: {
    rollupOptions: {
      output: {
        manualChunks: undefined,
      },
    },
    sourcemap: false,
  },
  esbuild: {
    charset: 'utf8',
  }
})

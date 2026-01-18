import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  //代理配置，这样一来前端发送的请求域名就是当前URL的域名
  server: {
    proxy: {
      //遇到/api开头的接口就会代理到后端
      '/api': 'http://localhost:8123',
    },
  },
})

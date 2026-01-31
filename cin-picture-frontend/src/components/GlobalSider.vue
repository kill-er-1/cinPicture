<template>
  <div id="globalSider">
    <a-layout-sider
      v-if="loginUserStore.loginUser.id"
      class="sider"
      width="200"
      breakpoint="lg"
      collapsed-width="0"
    >
      <a-menu
        mode="inline"
        v-model:selectedKeys="current"
        :items="menuItems"
        @click="doMenuClick"
      />
    </a-layout-sider>
  </div>
</template>

<script setup lang="ts">
import { PictureOutlined, UserOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const loginUserStore = useLoginUserStore()
const router = useRouter()

const current = ref<string[]>([])
const menuItems = ref<MenuProps['items']>([
  {
    key: '/',
    label: '公共图库',
    icon: () => h(PictureOutlined),
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined),
  },
])

const doMenuClick: MenuProps['onClick'] = ({ key }) => {
  router.push({
    path: key as string,
  })
}

// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  current.value = [to.path]
})
</script>

<style scoped>
#globalSider .sider {
  background: #fff;
}
</style>

<template>
  <div id="globalHeader">
    <!-- a-row a-col 是左中右布局 珊格 左右固定 中间自适应  warp不允许换行-->
    <a-row :wrap="false">
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.jpg" alt="logo" />
            <div class="title">cin云图库</div>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <!--
    1.这是 a-menu 组建的三个prop 由script里面的响应式变量传进去值
    2.v-model 是双向绑定 v-bind是单向绑定 缩写:
    3.selectedKeys是默认停在的菜单栏选项
    -->
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <!-- - - Ant Design Vue 的下拉组件：默认会把里面的“触发区域”作为点击/悬停触发点，展示一个浮层菜单。 -->
              <ASpace>
                <!-- Ant Design Vue 的间距容器，把里面的元素水平排列并自动加间距（这里是头像 + 文本）。 -->
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
                <!-- ?? 是 JS 的空值合并运算符，只在 null/undefined 时才用右边 -->
              </ASpace>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import { h, ref } from 'vue'
/*
  1.h 是创建Vnode 虚拟dom的函数
  2.ref是声明响应式变量
 */
import { HomeOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { userLogoutUsingPost } from '@/api/userController'
const router = useRouter()
const loginUserStore = useLoginUserStore()
const current = ref<string[]>([])
const items = ref<MenuProps['items']>([
  /*
  1. MenuProps['items'] 从MenuProps 中取出 item
  2.label是显示在界面的内容
  3.title是鼠标悬停时显示的内容
   */
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: 'others',
    label: h('a', { href: 'http://caide.xin', target: '_blank' }, 'cin'),
    //h(组件或标签, 属性对象, 子内容)
    title: 'cin',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
])

const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
}

router.afterEach((to) => {
  current.value = [to.path]
})

const doLogout = async () => {
  const res = await userLogoutUsingPost()
  console.log(res)
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败' + res.data.message)
  }
}
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>

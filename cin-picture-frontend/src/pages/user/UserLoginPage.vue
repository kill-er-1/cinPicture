<template>
  <div id = "userLoginPage">
    <h2 class="title">cin云图库 - 用户登录</h2>
    <div class="desc">企业级智能协同云图库</div>
    <a-form
      :model="formState"
      name="basic"
      autocomplete="off"
      @finish="handleSubmit"
    >
      <!-- model 绑定表单的数据模型 scprit里有定义 -->
      <!-- name 名字 -->
      <!-- :label-col="{ span: 8 }" 配置“label（左侧标题）”这一列的栅格宽度，占 24 栅格里的 8 份。 wrapper 输入控件（右侧内容）-->
      <!-- autocomplete="off"- 关闭浏览器对该表单的自动填充/自动完成（比如自动填用户名密码）。 -->
      <!-- @finish="onFinish"
- 表单校验通过并提交（点击 submit）时触发的事件回调。
- 这里的 onFinish(values) 会拿到表单值（通常是按 a-form-item 的 name 收集出来）。 -->
      <a-form-item
        name="userAccount"
        :rules="[{ required: true, message: '请输入账号' }]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[{ required: true, message: '请输入密码' },{min:8,message:'密码不少于8位'}]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码"/>
      </a-form-item>

      <div class="tips">
        没有账号?
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>

      <a-form-item >
        <a-button type="primary" html-type="submit" style="width: 100%;">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { userLoginUsingPost } from '@/api/userController';
import { useLoginUserStore } from '@/stores/useLoginUserStore';
import { message } from 'ant-design-vue';
import { reactive } from 'vue'
import { useRouter } from 'vue-router';
// 把一个普通对象变成“可被追踪的状态” 。一旦这个对象的属性被读/被写，Vue 就能知道“谁依赖了它、它变了之后该让谁更新”，从而自动触发组件重新渲染。
// 手里是一个“对象形态的状态”，而且会频繁读写它的多个字段时：

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 提交表单
const handleSubmit = async(values:any) => {
  const res = await userLoginUsingPost(values)
  //登录成功，把状态保存到全局
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path:'/',
      replace:true,
    })
  }else {
      message.error('登录失败' + res.data.message)
    }
}
</script>

<style scoped>
  #userLoginPage {
    max-width: 360px;
    margin:0 auto;
  }
  .title {
    text-align: center;
    margin-bottom: 16px;
  }
  .desc {
    text-align: center;
    color: #bbb;
    margin-bottom: 16px;
  }
  .tips {
    margin-bottom: 16px;
    color: #bbb;
    font-size: 13px;
    text-align: right;
  }
</style>

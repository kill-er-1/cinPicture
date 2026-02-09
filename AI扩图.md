### 前端开发

可以参考基础编辑图片的交互流程，在编辑图片按钮旁边添加 AI 扩图按钮，点击之后显示弹窗进行 AI 扩图操作。

这样可以将 AI 操作的逻辑封装到单独的组件中，让创建图片页面的代码更精简。NYk9omRRbMtb6eYnCAArAErDvMuLhxdFfhahbRdMVZs=

#### 1、AI 扩图弹窗

1）先复制之前开发好的裁剪图片弹窗，保留控制弹窗显示隐藏的逻辑，修改弹窗的标题：

```php-template
<template>
  <a-modal
    class="image-out-painting"
    v-model:visible="visible"
    title="AI 扩图"
    :footer="false"
    @cancel="closeModal"
  >

  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { uploadPictureUsingPost } from '@/api/pictureController'
import { message } from 'ant-design-vue'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal,
})
</script>

<style scoped>
.image-out-painting {
  text-align: center;
}
</style>
```

由于 AI 扩图一定是对已有图片进行编辑，所以弹窗的属性可以不需要 spaceId。

2）开发弹窗的内容，采用一行两列栅格布局，左边显示原始图片、右边显示扩图结果，下方展示扩图操作按钮。

```php-template
<a-row gutter="16">
  <a-col span="12">
    <h4>原始图片</h4>
    <img :src="picture?.url" :alt="picture?.name" style="max-width: 100%" />
  </a-col>
  <a-col span="12">
    <h4>扩图结果</h4>
    <img
      v-if="resultImageUrl"
      :src="resultImageUrl"
      :alt="picture?.name"
      style="max-width: 100%"
    />
  </a-col>
</a-row>
<div style="margin-bottom: 16px" />
<a-flex gap="16" justify="center">
  <a-button type="primary" ghost @click="createTask">生成图片</a-button>
  <a-button type="primary" @click="handleUpload">应用结果</a-button>
</a-flex>
```

定义变量，用于存储图片结果：

```csharp
const resultImageUrl = ref<string>()
```

3）编写创建任务函数：B2z6448PQSc3P5d8RibCpEJZ9ZaX16ayHXniF5A52nY=

```kotlin
// 任务 id
let taskId = ref<string>()

/**
 * 创建任务
 */
const createTask = async () => {
  if (!props.picture?.id) {
    return
  }
  const res = await createPictureOutPaintingTaskUsingPost({
    pictureId: props.picture.id,
    // 可以根据需要设置扩图参数
    parameters: {
      xScale: 2,
      yScale: 2,
    },
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建任务成功，请耐心等待，不要退出界面')
    console.log(res.data.data.output.taskId)
    taskId.value = res.data.data.output.taskId
    // 开启轮询
    startPolling()
  } else {
    message.error('创建任务失败，' + res.data.message)
  }
}
```

任务创建成功后，要开启轮询。

4）编写轮询逻辑。注意无论任务执行成功或失败、还是退出当前页面时，都需要执行清理逻辑，包括：sV0XXF40kQah+NcVxFP1y4nQNGtV/XyQvOt/FJKWUoA=

- 清理定时器
- 将定时器变量设置为 null
- 将任务 id 设置为 null，这样允许前端多次执行任务

代码如下：

```javascript
// 轮询定时器
let pollingTimer: NodeJS.Timeout = null

// 清理轮询定时器
const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = null
  }
}

// 开始轮询
const startPolling = () => {
  if (!taskId.value) return

  pollingTimer = setInterval(async () => {
    try {
      const res = await getPictureOutPaintingTaskUsingGet({
        taskId: taskId.value,
      })
      if (res.data.code === 0 && res.data.data) {
        const taskResult = res.data.data.output
        if (taskResult.taskStatus === 'SUCCEEDED') {
          message.success('扩图任务成功')
          resultImageUrl.value = taskResult.outputImageUrl
          clearPolling()
        } else if (taskResult.taskStatus === 'FAILED') {
          message.error('扩图任务失败')
          clearPolling()
        }
      }
    } catch (error) {
      console.error('轮询任务状态失败', error)
      message.error('检测任务状态失败，请稍后重试')
      clearPolling()
    }
  }, 3000) // 每隔 3 秒轮询一次
}

// 清理定时器，避免内存泄漏
onUnmounted(() => {
  clearPolling()
})
```

5）当任务执行成功后，可以得到图片结果，此时就可以点击 “应用结果” 按钮，调用图片 URL 上传接口。这段代码可以直接复制已开发的 URL 图片上传组件，补充 loading 效果：

```csharp
const uploadLoading = ref<boolean>(false)

const handleUpload = async () => {
  uploadLoading.value = true
  try {
    const params: API.PictureUploadRequest = {
      fileUrl: resultImageUrl.value,
      spaceId: props.spaceId,
    }
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      // 关闭弹窗
      closeModal()
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    uploadLoading.value = false
  }
}
```

6）适当对页面做一些优化。sV0XXF40kQah+NcVxFP1y4nQNGtV/XyQvOt/FJKWUoA=

给生成图片按钮添加任务执行的 loading 效果，有任务 id 时，禁止按钮点击，可以防止重复提交任务。扩图结束后，会清理 taskId，就可以再次执行。

```bash
<a-button type="primary" :loading="!!taskId" ghost
  @click="createTask">
  生成图片
</a-button>
```

2）添加应用结果（上传图片时）的 loading 效果：ChdipREN0jrjiXgJ/YkPo5ufCLwmKJhcbf8H+qMVE48=

```bash
<a-button type="primary" :loading="uploadLoading"
  @click="handleUpload">
  应用结果
</a-button>
```

3）有图片结果时才显示 “应用结果” 按钮：

```bash
<a-button type="primary" v-if="resultImageUrl"
  :loading="uploadLoading"
  @click="handleUpload">
  应用结果
</a-button>
```

#### 2、创建图片页面引入弹窗

在创建图片页面使用组件，可以在编辑图片按钮右侧增加 “AI 扩图”，点击按钮后打开弹窗：

```php-template
<a-space size="middle">
  <a-button :icon="h(EditOutlined)" @click="doEditPicture">编辑图片</a-button>
  <a-button type="primary" ghost :icon="h(FullscreenOutlined)" @click="doImagePainting">
    AI 扩图
  </a-button>
</a-space>
<ImageOutPainting
  ref="imageOutPaintingRef"
  :picture="picture"
  :spaceId="spaceId"
  :onSuccess="onImageOutPaintingSuccess"
/>
```

编辑点击按钮后触发的函数，打开弹窗：

```javascript
// AI 扩图弹窗引用
const imageOutPaintingRef = ref()

// AI 扩图
const doImagePainting = () => {
  if (imageOutPaintingRef.value) {
    imageOutPaintingRef.value.openModal()
  }
}

// 编辑成功事件
const onImageOutPaintingSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}
```

运行效果如图，感觉还是不错的吧~xo0dnOlnBf7Ws27/qGm2TPReUJyIxD/DuwJOjlvKUe0=

![](https://pic.code-nav.cn/course_picture/1608440217629360130/eEoc7fSoNi21yVXa.webp)

### 扩展知识 - 异步任务优化

异步任务管理其实算是一类经典业务场景，有许多通用的优化方法可以提高系统效率和用户体验。9JcB9KvGktsJeCekY//R96Nlv+uCpT8TeUU7sYgObzE=

1）任务队列和优先级

使用消息队列系统（比如 RabbitMQ、Kafka）对异步任务进行管理，可以根据优先级灵活调度任务。通过队列还可以限制同时处理的任务数量、削峰填谷，防止资源过载，提高系统稳定性。

2）任务记录和状态管理uPqbd3DiPd3iSPyF1TJ20nEUpYukqZektmm49mRYrTY=

现在用户是无法找到往期执行的任务和生成的图片的。可以设计任务记录表，存储每个任务的状态、结果和相关信息，并提供接口供用户查询历史任务。

前端可以给用户提供往期任务查询页面，能够查看任务结果、重试某一次任务等。还可以给管理员提供监控系统所有任务的页面，比如任务数、成功率和失败率，全面掌握任务执行情况。

实现起来并不难，其实就是对任务记录表的增删改查。9JcB9KvGktsJeCekY//R96Nlv+uCpT8TeUU7sYgObzE=

3）任务错误信息优化

完善任务失败的具体原因，帮助用户快速理解和解决问题。比如参数错误、图片格式不支持等。如果调用了第三方接口，需要认真阅读接口所有可能的错误情况。

4）计费与额度控制xo0dnOlnBf7Ws27/qGm2TPReUJyIxD/DuwJOjlvKUe0=

AI 扩图一般是计费业务，需要做好额度控制，并且仅登录用户才可以使用。

分享几个实现思路：

1.  在用户表中添加“扩图额度”（比如使用次数或余额），每次提交任务前先检查额度是否足够，额度不足则提示用户充值。
2.  每次任务提交时，可采用预扣费逻辑，任务完成扣费，任务失败则自动退还额度。
3.  提供查询用户当前剩余额度的接口，用户可以在前端看到自己剩余的额度。
4.  支持充值额度或会员订阅制收费，还可以根据扩图模式按比例扣费。比如普通模式扣 1 点，高清模式扣 2 点。

💡 一般对于后付费资源（随用随付费），即使余额 < 0，小额欠费也是可以接受的。尤其是对于大厂云服务来说，由于调用量巨大，很难做到实时计费。

5）安全性与稳定性

由于任务要消耗系统资源或成本，所以一定要设置合理的限流规则，防止恶意刷任务。比如限制单用户的任务提交频率，每分钟最多允许提交 3 次任务，超过限制后返回提示信息。9JcB9KvGktsJeCekY//R96Nlv+uCpT8TeUU7sYgObzE=

对于长耗时任务，还要设置任务的最大执行时间（比如 10 分钟），超时则自动标记任务失败。

鱼皮编程导航的 [智能 BI 项目](https://www.codefather.cn/course/1790980531403927553) 和 [面试鸭刷题平台项目](https://www.codefather.cn/course/1826803928691945473) 中都有讲解分布式限流相关的知识，可以按需学习。

此外，可以在任务执行前增加基础的校验，只对符合要求的图片创建任务，比如图片不能过大或过小：E2AG5YeWw8LgKsKYXs9Vrz+8AgTqaJUf5eaOZTC+BFg=

![](https://pic.code-nav.cn/course_picture/1608440217629360130/FeyVtS5agGJTmKeo.webp)

### 扩展

1、尝试更多 AI 图片处理能力，比如 [参考文档实现图配文](https://help.aliyun.com/zh/model-studio/developer-reference/image-text-composition-api-reference)sV0XXF40kQah+NcVxFP1y4nQNGtV/XyQvOt/FJKWUoA=

2、如果 AI 绘画 API 支持返回当前进度（比如 MidJourney 的 API），可以通过 SSE 的方式将进度返回给前端，鱼皮编程导航的 [AI 答题应用平台项目](https://www.codefather.cn/course/1790274408835506178) 中有关于 SSE 的实战。

3、优化 AI 扩图参数。可以 [参考官方文档](https://help.aliyun.com/zh/model-studio/developer-reference/image-scaling-api)，补充更多扩图参数，并允许用户自主选择扩图参数：

![](https://pic.code-nav.cn/course_picture/1608440217629360130/ygEsq3ZOmlw2lBfa.webp)E2AG5YeWw8LgKsKYXs9Vrz+8AgTqaJUf5eaOZTC+BFg=

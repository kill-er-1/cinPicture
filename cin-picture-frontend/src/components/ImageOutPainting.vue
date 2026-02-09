<template>
  <a-modal
    class="image-out-painting"
    v-model:visible="visible"
    title="AI 扩图"
    :footer="false"
    @cancel="closeModal"
  >
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
      <a-button
        type="primary"
        ghost
        :loading="!!taskId"
        :disabled="!!taskId"
        @click="createTask"
      >
        生成图片
      </a-button>
      <a-button
        type="primary"
        :loading="uploadLoading"
        :disabled="!resultImageUrl"
        @click="handleUpload"
      >
        应用结果
      </a-button>
    </a-flex>
    <a-typography-paragraph v-if="taskError" type="danger" style="margin-top: 12px">
      {{ taskError }}
    </a-typography-paragraph>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import {
  createPictureOutPaintingTaskUsingPost,
  getPictureOutPaintingTaskUsingGet,
  uploadPictureByUrlUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'

interface Props {
  picture?: API.PictureVO
  spaceId?: string
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

const visible = ref(false)
const resultImageUrl = ref<string>()
const taskId = ref<string | null>(null)
const uploadLoading = ref(false)
const taskError = ref<string>()

let pollingTimer: ReturnType<typeof setInterval> | null = null

const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = null
  }
}

const startPolling = () => {
  if (!taskId.value) return

  if (pollingTimer) {
    clearPolling()
  }

  pollingTimer = setInterval(async () => {
    try {
      const currentTaskId = taskId.value
      if (!currentTaskId) {
        return
      }
      const res = await getPictureOutPaintingTaskUsingGet({
        taskId: currentTaskId,
      })
      if (res.data.code === 0 && res.data.data) {
        const taskResult = res.data.data.output
        if (!taskResult) {
          return
        }
        if (taskResult.taskStatus === 'SUCCEEDED') {
          message.success('扩图任务成功')
          taskError.value = undefined
          resultImageUrl.value = taskResult.outputImageUrl
          clearPolling()
        } else if (taskResult.taskStatus === 'FAILED') {
          const detail = [taskResult.code, taskResult.message].filter(Boolean).join('：')
          taskError.value = detail || '扩图任务失败'
          message.error(taskError.value)
          clearPolling()
        }
      }
    } catch (error) {
      message.error('检测任务状态失败，请稍后重试')
      clearPolling()
    }
  }, 3000)
}

const createTask = async () => {
  if (!props.picture?.id) {
    return
  }
  clearPolling()
  taskError.value = undefined
  resultImageUrl.value = undefined
  const res = await createPictureOutPaintingTaskUsingPost({
    pictureId: props.picture.id,
    parameters: {
      xScale: 2,
      yScale: 2,
    },
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建任务成功，请耐心等待，不要退出界面')
    const newTaskId = res.data.data.output?.taskId
    if (!newTaskId) {
      message.error('创建任务失败，未获取到任务 id')
      return
    }
    taskId.value = newTaskId
    startPolling()
  } else {
    message.error('创建任务失败，' + res.data.message)
  }
}

const handleUpload = async () => {
  if (!resultImageUrl.value) return

  uploadLoading.value = true
  try {
    const params: API.PictureUploadRequest = {
      fileUrl: resultImageUrl.value,
      spaceId: props.spaceId as any,
    }
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      props.onSuccess?.(res.data.data)
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

const openModal = () => {
  visible.value = true
  taskError.value = undefined
  resultImageUrl.value = undefined
  clearPolling()
}

const closeModal = () => {
  visible.value = false
  taskError.value = undefined
  resultImageUrl.value = undefined
  clearPolling()
}

onUnmounted(() => {
  clearPolling()
})

defineExpose({
  openModal,
})
</script>

<style scoped>
.image-out-painting {
  text-align: center;
}
</style>

<template>
  <a-row :gutter="[16, 16]">
    <!-- 图片展示区 -->
    <a-col :sm="24" :md="16" :xl="18">
      <a-card title="图片预览">
        <a-image style="max-height: 600px; object-fit: contain" :src="picture.url" />
      </a-card>
    </a-col>
    <!-- 图片信息区 -->
    <a-col :sm="24" :md="8" :xl="6">
      <a-card title="图片信息">
        <a-descriptions :column="1">
          <a-descriptions-item label="作者">
            <a-space>
              <a-avatar :size="24" :src="picture.user?.userAvatar" />
              <div>{{ picture.user?.userName }}</div>
            </a-space>
          </a-descriptions-item>
          <a-descriptions-item label="名称">
            {{ picture.name ?? '未命名' }}
          </a-descriptions-item>
          <a-descriptions-item label="简介">
            {{ picture.introduction ?? '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="分类">
            {{ picture.category ?? '默认' }}
          </a-descriptions-item>
          <a-descriptions-item label="标签">
            <a-tag v-for="tag in picture.tags" :key="tag">
              {{ tag }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="格式">
            {{ picture.picFormat ?? '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="宽度">
            {{ picture.picWidth ?? '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="高度">
            {{ picture.picHeight ?? '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="宽高比">
            {{ picture.picScale ?? '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="大小">
            {{ formatSize(picture.picSize) }}
          </a-descriptions-item>
        </a-descriptions>
        <!-- 图片操作 -->
        <a-space wrap>
          <a-button type="primary" @click="doDownload">
            免费下载
            <template #icon>
              <DownloadOutlined />
            </template>
          </a-button>
          <a-button :icon="h(ShareAltOutlined)" type="primary" ghost @click="doShare">
            分享
          </a-button>
          <a-button v-if="canEdit" :icon="h(EditOutlined)" type="default" @click="doEdit">
            编辑
          </a-button>
          <a-button v-if="canDelete" :icon="h(DeleteOutlined)" danger @click="doDelete">
            删除
          </a-button>
        </a-space>
      </a-card>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { getPictureVoByIdUsingGet } from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { computed, h, onMounted, ref } from 'vue'
import { downloadImage, formatSize } from '@/utils'
const props = defineProps<{
  id: string | number
}>()

const picture = ref<API.PictureVO>({})

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: String(props.id),
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败：' + e.message)
  }
}

import { deletePictureUsingPost } from '@/api/pictureController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { DeleteOutlined, DownloadOutlined, EditOutlined, ShareAltOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginUserStore = useLoginUserStore()
// 是否具有编辑权限
const canEdit = computed(() => {
  const loginUser = loginUserStore.loginUser
  // 未登录不可编辑
  if (!loginUser.id) {
    return false
  }
  // 仅本人或管理员可编辑
  const user = picture.value.user || {}
  return loginUser.id === user.id || loginUser.userRole === 'admin'
})

const canDelete = computed(() => canEdit.value)

const doShare = async () => {
  const url = window.location.href
  try {
    await navigator.clipboard.writeText(url)
    message.success('链接已复制')
  } catch (e) {
    message.error('复制失败')
  }
}

// 编辑
const doEdit = () => {
  router.push({
    path: '/add_picture',
    query: {
      id: picture.value.id,
      spaceId: picture.value.spaceId,
    },
  })
}
// 删除
const doDelete = async () => {
  const id = picture.value.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id: id as any })
  if (res.data.code === 0) {
    message.success('删除成功')
  } else {
    message.error('删除失败')
  }
}

// 处理下载
const doDownload = () => {
  downloadImage(picture.value.url)
}
onMounted(() => {
  fetchPictureDetail()
})
</script>

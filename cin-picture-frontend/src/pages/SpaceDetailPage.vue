<template>
  <div id="spaceDetailPage">
    <a-flex justify="space-between">
      <h2>{{ space.spaceName }}（私有空间）</h2>
      <a-space size="middle">
        <a-button type="primary" :href="`/add_picture?spaceId=${id}`" target="_blank">
          + 创建图片
        </a-button>
        <a-button
          type="primary"
          ghost
          :icon="h(BarChartOutlined)"
          :href="`/space_analyze?spaceId=${id}`"
          target="_blank"
        >
          空间分析
        </a-button>
        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <a-progress type="circle" :percent="spaceUsagePercent" :size="42" />
        </a-tooltip>
      </a-space>
    </a-flex>

    <PictureList :dataList="dataList" :loading="loading" showOp :onReload="fetchData" />
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      :show-total="() => `图片总数 ${total} / ${space.maxCount ?? 0}`"
      @change="onPageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { getSpaceVoByIdUsingGet } from '@/api/spaceController'
import { listPictureVoByPageUsingPost } from '@/api/pictureController'
import PictureList from '@/components/PictureList.vue'
import { formatSize } from '@/utils'
import { message } from 'ant-design-vue'
import { BarChartOutlined } from '@ant-design/icons-vue'
import { h } from 'vue'
import { computed, onMounted, reactive, ref } from 'vue'

const props = defineProps<{
  id: string | number
}>()

const space = ref<API.SpaceVO>({})

const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: String(props.id) as any,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败：' + e.message)
  }
}

const dataList = ref<API.PictureVO[]>([])
const total = ref<number>(0)
const loading = ref<boolean>(true)

const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  const params: API.PictureQueryRequest = {
    spaceId: String(props.id),
    ...searchParams,
  }
  console.log('查询图片参数：', params)
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = Number(res.data.data.total) ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

const spaceUsagePercent = computed(() => {
  const maxSize = space.value.maxSize ?? 0
  if (!maxSize) {
    return 0
  }
  const totalSize = space.value.totalSize ?? 0
  return Math.min(100, Number(((totalSize * 100) / maxSize).toFixed(1)))
})

onMounted(() => {
  fetchSpaceDetail()
  fetchData()
})
</script>

<template>
  <div class="space-user-analyze">
    <a-flex gap="middle">
      <a-card title="存储空间" style="width: 50%">
        <div style="height: 320px; text-align: center">
          <h3>
            {{ formatSize(data.usedSize) }}/
            {{ data.maxSize ? formatSize(data.maxSize) : '无限制' }}
          </h3>
          <a-progress type="dashboard" :percent="data.sizeUsageRatio ?? 0" />
        </div>
      </a-card>
      <a-card title="图片数量" style="width: 50%">
        <div style="height: 320px; text-align: center">
          <h3>{{ data.usedCount }} / {{ data.maxCount ?? '无限制' }}</h3>
          <a-progress type="dashboard" :percent="data.countUsageRatio ?? 0" />
        </div>
      </a-card>
    </a-flex>
  </div>
</template>

<script lang="ts" setup>
import { ref, watchEffect } from 'vue'
import { getSpaceUsageAnalyzeUsingPost } from '@/api/spaceAnalyzeController'
import { message } from 'ant-design-vue'
import { formatSize } from '@/utils'
interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: string | number
}
const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

//数据
const data = ref<API.SpaceUsageAnalyzeResponse>({})
const loading = ref(false)

//获取数据
const fetchData = async () => {
  loading.value = true
  const body = {
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    // Long id: keep string to avoid JS precision loss
    spaceId: props.spaceId,
  } satisfies Omit<API.SpaceUsageAnalyzeRequest, 'spaceId'> & { spaceId?: string | number }

  const res = await getSpaceUsageAnalyzeUsingPost(body as unknown as API.SpaceUsageAnalyzeRequest)
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data
  } else {
    message.error('获取空间资源使用分析数据失败：' + res.data.message)
  }
  loading.value = false
}

watchEffect(() => {
  fetchData()
})
//加载状态
</script>

<style scoped></style>

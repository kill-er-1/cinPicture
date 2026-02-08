<template>
  <div class="space-size-analyze">
    <a-card title="空间图片大小分析">
      <VChart :option="options" style="height: 320px" max-width="100%" :loading="loading" />
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import VChart from 'vue-echarts'
import 'echarts'
import { ref, watchEffect, computed } from 'vue'
import { getSpaceSizeAnalyzeUsingPost } from '@/api/spaceAnalyzeController'
import { message } from 'ant-design-vue'
import 'echarts'
import 'echarts-wordcloud'

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
const dataList = ref<API.SpaceSizeAnalyzeResponse[]>([])
const loading = ref(false)

//获取数据
const fetchData = async () => {
  loading.value = true
  const body = {
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    // Long id: keep string to avoid JS precision loss
    spaceId: props.spaceId,
  } satisfies Omit<API.SpaceSizeAnalyzeRequest, 'spaceId'> & { spaceId?: string | number }

  const res = await getSpaceSizeAnalyzeUsingPost(body as unknown as API.SpaceSizeAnalyzeRequest)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取空间图片分类分析数据失败：' + res.data.message)
  }
  loading.value = false
}

watchEffect(() => {
  fetchData()
}) //图标选项
const options = computed(() => {
  const pieData = dataList.value.map((item) => ({
    name: item.sizeRange,
    value: item.count,
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      top: 'bottom',
    },
    series: [
      {
        name: '图片大小',
        type: 'pie',
        radius: '50%',
        data: pieData,
      },
    ],
  }
})
//加载状态
</script>

<style scoped></style>

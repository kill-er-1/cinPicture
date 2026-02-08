<template>
  <div class="space-user-analyze">
    <a-card title="空间图片用户分析">
      <v-chart :option="options" style="height: 320px" />
      <template #extra>
        <a-space>
          <a-segmented v-model:value="timeDimension" :options="timeDimensionOptions" />
          <a-input-search placeholder="请输入用户 id" enter-button="搜索用户" @search="doSearch" />
        </a-space>
      </template>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import VChart from 'vue-echarts'
import 'echarts'
import { ref, watchEffect, computed } from 'vue'
import { getSpaceUserAnalyzeUsingPost } from '@/api/spaceAnalyzeController'
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
const dataList = ref<API.SpaceUserAnalyzeResponse[]>([])
const loading = ref(false)
const userId = ref<string>('')
const timeDimension = ref<string>('day')
const timeDimensionOptions = [
  {
    label: '日',
    value: 'day',
  },
  {
    label: '周',
    value: 'week',
  },
  {
    label: '月',
    value: 'month',
  },
]

const doSearch = (value: string) => {
  userId.value = value
}
type SpaceUserAnalyzeBody = Omit<API.SpaceUserAnalyzeRequest, 'spaceId' | 'userId'> & {
  spaceId?: string | number
  userId?: string
}

const fetchData = async () => {
  loading.value = true
  const body = {
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
    timeDimension: timeDimension.value,
    userId: userId.value || undefined,
  } satisfies SpaceUserAnalyzeBody

  const res = await getSpaceUserAnalyzeUsingPost(body as unknown as API.SpaceUserAnalyzeRequest)
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
  const periods = dataList.value.map((item) => item.period) // 时间区间
  const counts = dataList.value.map((item) => item.count) // 上传数量

  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: periods, name: '时间区间' },
    yAxis: { type: 'value', name: '上传数量' },
    series: [
      {
        name: '上传数量',
        type: 'line',
        data: counts,
        smooth: true, // 平滑折线
        emphasis: {
          focus: 'series',
        },
      },
    ],
  }
})
//加载状态
</script>

<style scoped></style>

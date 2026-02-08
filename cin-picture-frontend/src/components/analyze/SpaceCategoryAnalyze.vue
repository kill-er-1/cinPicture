<template>
  <div class="space-category-analyze">
    <a-card title="空间图片分类分析">
      <VChart :option="options" style="height: 320px" max-width="100%" :loading="loading" />
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import VChart from 'vue-echarts'
import 'echarts'
import { ref, watchEffect, computed } from 'vue'
import { getSpaceCategoryAnalyzeUsingPost } from '@/api/spaceAnalyzeController'
import { message } from 'ant-design-vue'

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
const dataList = ref<API.SpaceCategoryAnalyzeResponse[]>([])
const loading = ref(false)

//获取数据
const fetchData = async () => {
  loading.value = true
  const body = {
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    // Long id: keep string to avoid JS precision loss
    spaceId: props.spaceId,
  } satisfies Omit<API.SpaceCategoryAnalyzeRequest, 'spaceId'> & { spaceId?: string | number }

  const res = await getSpaceCategoryAnalyzeUsingPost(
    body as unknown as API.SpaceCategoryAnalyzeRequest,
  )
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
  const categories = dataList.value.map((item) => item.category ?? '未分类')
  const countData = dataList.value.map((item) => item.count ?? 0)
  const sizeData = dataList.value.map((item) =>
    Number((((item.totalSize ?? 0) as number) / (1024 * 1024)).toFixed(2)),
  ) // 转为 MB

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['图片数量', '图片总大小'], top: 'bottom' },
    xAxis: { type: 'category', data: categories },
    yAxis: [
      {
        type: 'value',
        name: '图片数量',
        axisLine: { show: true, lineStyle: { color: '#5470C6' } }, // 左轴颜色
      },
      {
        type: 'value',
        name: '图片总大小 (MB)',
        position: 'right',
        axisLine: { show: true, lineStyle: { color: '#91CC75' } }, // 右轴颜色
        splitLine: {
          lineStyle: {
            color: '#91CC75', // 调整网格线颜色
            type: 'dashed', // 线条样式：可选 'solid', 'dashed', 'dotted'
          },
        },
      },
    ],
    series: [
      { name: '图片数量', type: 'bar', data: countData, yAxisIndex: 0 },
      { name: '图片总大小', type: 'bar', data: sizeData, yAxisIndex: 1 },
    ],
  }
})
//加载状态
</script>

<style scoped></style>

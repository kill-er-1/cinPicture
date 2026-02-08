<template>
  <div class="space-tag-analyze">
    <a-card title="图库标签词云">
      <VChart :option="options" style="height: 320px" max-width="100%" :loading="loading" />
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import VChart from 'vue-echarts'
import 'echarts'
import { ref, watchEffect, computed } from 'vue'
import { getSpaceTagAnalyzeUsingPost } from '@/api/spaceAnalyzeController'
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
const dataList = ref<API.SpaceTagAnalyzeResponse[]>([])
const loading = ref(false)

//获取数据
const fetchData = async () => {
  loading.value = true
  const body = {
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    // Long id: keep string to avoid JS precision loss
    spaceId: props.spaceId,
  } satisfies Omit<API.SpaceTagAnalyzeRequest, 'spaceId'> & { spaceId?: string | number }

  const res = await getSpaceTagAnalyzeUsingPost(body as unknown as API.SpaceTagAnalyzeRequest)
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
  const tagData = dataList.value.map((item) => ({
    name: item.tag,
    value: item.count,
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => `${params.name}: ${params.value} 次`,
    },
    series: [
      {
        type: 'wordCloud',
        gridSize: 10,
        sizeRange: [12, 50], // 字体大小范围
        rotationRange: [-90, 90],
        shape: 'circle',
        textStyle: {
          color: () =>
            `rgb(${Math.round(Math.random() * 255)}, ${Math.round(
              Math.random() * 255,
            )}, ${Math.round(Math.random() * 255)})`, // 随机颜色
        },
        data: tagData,
      },
    ],
  }
})
//加载状态
</script>

<style scoped></style>

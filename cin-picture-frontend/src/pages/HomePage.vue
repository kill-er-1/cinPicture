<template>
  <!-- 搜索框 -->
  <div class="search-bar">
    <a-input-search
      placeholder="从海量图片中搜索"
      v-model:value="searchParams.searchText"
      enter-button="搜索"
      size="large"
      @search="doSearch"
    />
  </div>
  <!-- 分类 + 标签 -->
  <a-tabs v-model:activeKey="selectedCategory" @change="doSearch">
    <a-tab-pane key="all" tab="全部" />
    <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" />
  </a-tabs>
  <div class="tag-bar">
    <span style="margin-right: 8px">标签：</span>
    <a-space :size="[0, 8]" wrap>
      <a-checkable-tag
        v-for="(tag, index) in tagList"
        :key="tag"
        v-model:checked="selectedTagList[index]"
        @change="doSearch"
      >
        {{ tag }}
      </a-checkable-tag>
    </a-space>
  </div>
  <!-- 图片列表 -->
  <PictureList :dataList="dataList" :loading="loading" />
  <a-pagination
    style="text-align: right"
    v-model:current="searchParams.current"
    v-model:pageSize="searchParams.pageSize"
    :total="total"
    @change="onPageChange"
  />
</template>

<script setup lang="ts">
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import PictureList from '@/components/PictureList.vue'
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const dataList = ref<API.PictureVO[]>([])
const total = ref<number>(0)
const loading = ref<boolean>(true)

const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

//获取数据
const fetchData = async () => {
  loading.value = true

  //转换搜索参数
  const params: API.PictureQueryRequest & { tags: string[] } = {
    ...searchParams,
    tags: [],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })

  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败' + res.data.message)
  }
  loading.value = false
}

const route = useRoute()

const syncSpaceFilterFromRoute = () => {
  const spaceIdQuery = route.query.spaceId
  const spaceId = Array.isArray(spaceIdQuery) ? spaceIdQuery[0] : spaceIdQuery

  if (spaceId) {
    // 避免超大 Long 精度丢失：不要 Number()
    searchParams.spaceId = String(spaceId) as any
    searchParams.nullSpaceId = undefined
    return
  }

  const nullSpaceIdQuery = route.query.nullSpaceId
  const nullSpaceIdVal = Array.isArray(nullSpaceIdQuery)
    ? nullSpaceIdQuery[0]
    : nullSpaceIdQuery

  searchParams.spaceId = undefined
  searchParams.nullSpaceId = nullSpaceIdVal ? nullSpaceIdVal !== 'false' : true
}



const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

watch(
  () => [route.query.spaceId, route.query.nullSpaceId],
  () => {
    syncSpaceFilterFromRoute()
    doSearch()
  },
  { immediate: true }
)

//获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    //转换成下拉选项组建接受的格式
    categoryList.value = res.data.data.categoryList ?? []
    tagList.value = res.data.data.tagList ?? []
  } else {
    message.error('加载分类标签失败 ' + res.data.message)
  }
}
onMounted(() => {
  getTagCategoryOptions()
})

</script>

<style scoped>
.search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}
</style>

<script setup lang="ts">
import { editPictureUsingPost, listPictureTagCategoryUsingGet,getPictureVoByIdUsingGet } from '@/api/pictureController'
import PictureUpload from '@/components/PictureUpload.vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

type Option = { value: string; label: string }

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const categoryOptions = ref<Option[]>([])
const tagOptions = ref<Option[]>([])

const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

const router = useRouter()

const handleSubmit = async (values: any) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    ...values,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success(isEdit.value ? '修改成功' : '创建成功')
    router.push({
      path: `/picture/${pictureId}`,
      // 上传成功跳转
    })
  } else {
    message.error((isEdit.value ? '修改失败' : '创建失败') + res.data.message)
  }
}

//获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('加载选项失败 ' + res.data.message)
  }
}

const route = useRoute()
const isEdit = computed(() => !!route.query?.id)

// 获取老数据
const getOldPicture = async () => {
  const rawId = route.query?.id
  const id = Array.isArray(rawId) ? rawId[0] : rawId
  if (!id) {
    return
  }
  const res = await getPictureVoByIdUsingGet({
    id,
  })
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data
    picture.value = data
    pictureForm.name = data.name
    pictureForm.introduction = data.introduction
    pictureForm.category = data.category
    pictureForm.tags = data.tags
  }
}

onMounted(() => {
  getOldPicture()
})

onMounted(() => {
  getTagCategoryOptions()
})
</script>
<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ isEdit ? '修改图片' : '创建图片' }}
    </h2>
    <PictureUpload :picture="picture" :onSuccess="onSuccess" />
    <a-form v-if="picture" layout="vertical" :model="pictureForm" @finish="handleSubmit">
      <a-form-item label="名称" name="name">
        <a-input v-model:value="pictureForm.name" placeholder="请输入名称" />
      </a-form-item>
      <a-form-item label="简介" name="introduction">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          :rows="2"
          autoSize
          allowClear
        />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="pictureForm.category"
          :options="categoryOptions"
          placeholder="请输入分类"
          allowClear
        />
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="pictureForm.tags"
          :options="tagOptions"
          mode="tags"
          placeholder="请输入标签"
          allowClear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">
          {{ isEdit ? '修改' : '创建' }}
        </a-button>
      </a-form-item>
    </a-form>
  </div>
  <!-- 组建导入才能显示 -->
</template>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>

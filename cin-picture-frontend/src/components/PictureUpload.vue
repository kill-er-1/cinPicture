<script setup lang="ts">
import type { UploadProps } from 'ant-design-vue';
import {message} from 'ant-design-vue'
import {PlusOutlined,LoadingOutlined} from '@ant-design/icons-vue';
import {ref } from 'vue';
import { uploadPictureUsingPost } from '@/api/pictureController';
interface Props {
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
}
const props = defineProps<Props>();
const loading = ref<boolean>(false);

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('不支持上传该格式的图片,推荐jpg 或 png');
  }
  const isLt2M = file.size /1024 /1024 <2;
  if (!isLt2M) {
    message.error('不能超过 2M 的图片')
  }
  return isJpgOrPng && isLt2M;
}

const handleUpload = async({file}:any) => {
  loading.value = true;
  try {
    const params: API.uploadPictureUsingPOSTParams = props.picture?.id ? { id: props.picture.id } : {};

    const res = await uploadPictureUsingPost(params, {}, file);
    const baseRes = res.data as API.BaseResponsePictureVO_

    if (baseRes.code === 0 && baseRes.data) {
      message.success('图片上传成功');
      props.onSuccess?.(baseRes.data);
    } else {
      message.error('图片上传失败' + baseRes.message);
    }
  } catch (error) {
    message.error('图片上传失败');
  } finally {
    //无论上传失败都要关闭loading
    loading.value = false;
  }
}
</script>

<template>
  <div class="picture-upload">
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
    >
    <img v-if="picture?.url" :src="picture?.url" alt="avatar">

    <div v-else>
      <loading-outlined v-if="loading"></loading-outlined>
      <plus-outlined v-else></plus-outlined>
      <div class="ant-upload-text">点击或拖拽上传图片</div>
    </div>

    </a-upload>
  </div>
</template>

<style scoped>
.picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
}

.picture-upload img {
  max-width: 100%;
  max-height: 480px; /* 限制最大高度，防止图片过长 */
}

/* 图标样式 */
.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
</style>

<!-- filepath: /Users/cin/code/project/cin-picture-backend/cin-picture-frontend/src/views/HomeView.vue -->
<script setup lang="ts">
// ...existing code...
import { onMounted } from 'vue'
import { healthUsingGet } from '@/api/mainController'

onMounted(async () => {
  try {
    const res = await healthUsingGet()
    console.log('healthUsingGet res =', res)
  } catch (e) {
    console.error('healthUsingGet error =', e)
  }
})
// ...existing code...
</script>

<template>
  <!-- ...existing code... -->
  <div>home</div>
</template>

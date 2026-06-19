<template>
  <div class="container text-center" style="max-width:480px; padding-top:4rem;">
    <div class="card">
      <div style="font-size:3rem; margin-bottom:1rem;">❌</div>
      <h1 class="page-title mb-4">결제에 실패했습니다</h1>
      <p class="text-muted mb-2">{{ route.query.message || '결제 처리 중 오류가 발생했습니다.' }}</p>
      <p class="text-muted mb-4" style="font-size:.8rem;">오류 코드: {{ route.query.code || '-' }}</p>
      <div class="flex gap-2">
        <button class="btn btn-outline" style="flex:1;" @click="$router.back()">이전으로</button>
        <RouterLink to="/" class="btn btn-primary" style="flex:1;">홈으로</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

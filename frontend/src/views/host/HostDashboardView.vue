<template>
  <div class="container" style="max-width:900px;">
    <div class="page-header flex justify-between items-center">
      <h1 class="page-title">호스트 대시보드</h1>
      <RouterLink to="/host/accommodations/new" class="btn btn-primary">+ 숙소 등록</RouterLink>
    </div>

    <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

    <div v-else-if="accommodations.length === 0" class="text-center text-muted" style="padding:3rem 0;">
      등록한 숙소가 없습니다.
      <br /><br />
      <RouterLink to="/host/accommodations/new" class="btn btn-primary">첫 숙소 등록하기</RouterLink>
    </div>

    <div v-else class="accommodation-list">
      <div v-for="acc in accommodations" :key="acc.id" class="card accommodation-item">
        <div class="acc-header">
          <div>
            <h3 class="acc-name">{{ acc.name }}</h3>
            <p class="text-muted" style="font-size:.875rem;">{{ acc.sido }} {{ acc.sigungu }}</p>
          </div>
          <div class="flex gap-2 items-center">
            <span :class="accStatusBadge(acc.status)">{{ accStatusLabel(acc.status) }}</span>
            <RouterLink :to="`/host/accommodations/${acc.id}/edit`" class="btn btn-outline btn-sm">수정</RouterLink>
            <button class="btn btn-danger btn-sm" @click="handleDelete(acc.id)">삭제</button>
          </div>
        </div>

        <p class="text-muted" style="font-size:.875rem; margin-top:.5rem;">
          {{ acc.description?.slice(0, 80) }}{{ acc.description?.length > 80 ? '...' : '' }}
        </p>

        <RouterLink :to="`/accommodations/${acc.id}`" class="text-muted" style="font-size:.8rem; margin-top:.5rem; display:block;">
          숙소 상세 보기 →
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { accommodationApi } from '@/api/accommodation.js'

const accommodations = ref([])
const loading = ref(true)

const STATUS_LABEL = { ACTIVE: '운영중', INACTIVE: '비활성', DELETED: '삭제됨' }
const STATUS_BADGE = {
  ACTIVE: 'badge badge-green', INACTIVE: 'badge badge-yellow', DELETED: 'badge badge-red'
}
function accStatusLabel(s) { return STATUS_LABEL[s] ?? s }
function accStatusBadge(s) { return STATUS_BADGE[s] ?? 'badge badge-gray' }

async function loadAccommodations() {
  loading.value = true
  try {
    const res = await accommodationApi.getMyAccommodations()
    accommodations.value = res.data.content ?? res.data ?? []
  } finally {
    loading.value = false
  }
}

async function handleDelete(id) {
  if (!confirm('숙소를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return
  try {
    await accommodationApi.delete(id)
    await loadAccommodations()
  } catch (e) {
    alert(e.response?.data?.message ?? '삭제에 실패했습니다.')
  }
}

onMounted(loadAccommodations)
</script>

<style scoped>
.accommodation-list { display: flex; flex-direction: column; gap: 1rem; }
.acc-header { display: flex; justify-content: space-between; align-items: flex-start; }
.acc-name { font-weight: 600; font-size: 1.05rem; }
.btn-sm { padding: .375rem .875rem; font-size: .875rem; }
</style>

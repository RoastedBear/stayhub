<template>
  <div class="container">
    <!-- 검색 폼 -->
    <div class="card search-card mb-4">
      <h2 style="font-size:1.25rem; font-weight:700; margin-bottom:1rem;">숙소 검색</h2>
      <form @submit.prevent="search" class="search-form">
        <div class="form-group">
          <label class="form-label">지역 (시/도)</label>
          <input v-model="params.sido" type="text" placeholder="서울, 부산..." />
        </div>
        <div class="form-group">
          <label class="form-label">시/군/구</label>
          <input v-model="params.sigungu" type="text" placeholder="강남구..." />
        </div>
        <div class="form-group">
          <label class="form-label">체크인</label>
          <input v-model="params.checkInDate" type="date" :min="today" />
        </div>
        <div class="form-group">
          <label class="form-label">체크아웃</label>
          <input v-model="params.checkOutDate" type="date" :min="params.checkInDate || today" />
        </div>
        <div class="form-group">
          <label class="form-label">인원 수</label>
          <input v-model.number="params.guestCount" type="number" min="1" placeholder="2" />
        </div>
        <button type="submit" class="btn btn-primary search-btn">검색</button>
      </form>
    </div>

    <!-- 검색 결과 -->
    <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

    <div v-else-if="rooms.length === 0 && searched" class="text-center text-muted" style="padding:3rem 0;">
      검색 결과가 없습니다.
    </div>

    <div v-else-if="rooms.length > 0">
      <h3 class="page-header page-title">검색 결과 {{ rooms.length }}건</h3>
      <div class="grid-cards">
        <RouterLink
          v-for="room in rooms"
          :key="room.id"
          :to="`/accommodations/${room.accommodationId}`"
          class="room-card"
        >
          <div class="room-card-body">
            <p class="room-name">{{ room.name }}</p>
            <p class="room-accommodation">{{ room.accommodationName }}</p>
            <p class="text-muted" style="font-size:.8rem;">{{ room.sido }} {{ room.sigungu }}</p>
            <p class="room-price">{{ room.basePrice?.toLocaleString() }}원 / 박</p>
            <p class="text-muted" style="font-size:.8rem;">최대 {{ room.maxOccupancy }}명</p>
          </div>
        </RouterLink>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="pagination mt-6">
        <button class="btn btn-outline" :disabled="page === 0" @click="changePage(page - 1)">이전</button>
        <span class="text-muted">{{ page + 1 }} / {{ totalPages }}</span>
        <button class="btn btn-outline" :disabled="page >= totalPages - 1" @click="changePage(page + 1)">다음</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { accommodationApi } from '@/api/accommodation.js'

const today = new Date().toISOString().split('T')[0]

const params = ref({ sido: '', sigungu: '', checkInDate: '', checkOutDate: '', guestCount: null })
const rooms = ref([])
const loading = ref(false)
const searched = ref(false)
const page = ref(0)
const totalPages = ref(0)

async function search(p = 0) {
  loading.value = true
  searched.value = true
  page.value = p
  try {
    const query = {}
    if (params.value.sido) query.sido = params.value.sido
    if (params.value.sigungu) query.sigungu = params.value.sigungu
    if (params.value.checkInDate) query.checkInDate = params.value.checkInDate
    if (params.value.checkOutDate) query.checkOutDate = params.value.checkOutDate
    if (params.value.guestCount) query.guestCount = params.value.guestCount
    query.page = p
    query.size = 12

    const res = await accommodationApi.searchRooms(query)
    rooms.value = res.data.content ?? res.data
    totalPages.value = res.data.totalPages ?? 1
  } catch (e) {
    rooms.value = []
  } finally {
    loading.value = false
  }
}

function changePage(p) {
  search(p)
}
</script>

<style scoped>
.search-card { margin-top: 1rem; }
.search-form {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: .75rem;
  align-items: end;
}
.search-btn { height: 38px; }

.room-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: box-shadow .2s;
  display: block;
}
.room-card:hover { box-shadow: var(--shadow-md); }
.room-card-body { padding: 1rem; }
.room-name { font-weight: 600; font-size: 1rem; margin-bottom: .25rem; }
.room-accommodation { color: var(--text-muted); font-size: .875rem; margin-bottom: .25rem; }
.room-price { font-weight: 700; color: var(--primary); font-size: 1.1rem; margin-top: .5rem; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 1rem; }
</style>

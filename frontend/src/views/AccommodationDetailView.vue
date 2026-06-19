<template>
  <div class="container" style="max-width:900px;">
    <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

    <div v-else-if="accommodation">
      <!-- 숙소 기본 정보 -->
      <div class="page-header">
        <h1 class="page-title">{{ accommodation.name }}</h1>
        <p class="text-muted">{{ accommodation.sido }} {{ accommodation.sigungu }} {{ accommodation.address }}</p>
        <span class="badge badge-blue">{{ typeLabel(accommodation.type) }}</span>
        <span class="ml-2 text-muted" style="font-size:.875rem;">
          ★ {{ rating.averageRating ?? '-' }} ({{ rating.reviewCount }}개 리뷰)
        </span>
      </div>

      <div class="card mb-4">
        <h2 class="section-title">숙소 소개</h2>
        <p style="white-space:pre-wrap; line-height:1.7;">{{ accommodation.description }}</p>
      </div>

      <!-- 객실 목록 -->
      <h2 class="section-title mb-4">객실 선택</h2>
      <div v-if="rooms.length === 0" class="text-muted">등록된 객실이 없습니다.</div>
      <div class="grid-cards mb-4">
        <div v-for="room in rooms" :key="room.id" class="room-item card">
          <h3 style="font-weight:600; margin-bottom:.5rem;">{{ room.name }}</h3>
          <p class="text-muted" style="font-size:.875rem; margin-bottom:.5rem;">{{ room.description }}</p>
          <p class="text-muted" style="font-size:.8rem;">최대 {{ room.maxOccupancy }}명</p>
          <p class="room-price">{{ room.basePrice?.toLocaleString() }}원 / 박</p>
          <button class="btn btn-primary mt-4 w-full" @click="goReserve(room)">예약하기</button>
        </div>
      </div>

      <!-- 리뷰 목록 -->
      <div class="card">
        <h2 class="section-title">리뷰 ({{ rating.reviewCount }})</h2>
        <div v-if="reviews.length === 0" class="text-muted mt-4">아직 리뷰가 없습니다.</div>
        <div v-for="review in reviews" :key="review.reviewId" class="review-item">
          <div class="review-header">
            <span class="review-author">{{ review.guestName }}</span>
            <span class="review-rating">★ {{ review.rating }}</span>
            <span class="text-muted" style="font-size:.75rem;">{{ formatDate(review.createdAt) }}</span>
          </div>
          <p class="review-content">{{ review.content }}</p>
        </div>
        <button v-if="hasMoreReviews" class="btn btn-outline mt-4" @click="loadMoreReviews">
          더 보기
        </button>
      </div>
    </div>

    <div v-else class="text-center text-muted" style="padding:4rem 0;">숙소를 찾을 수 없습니다.</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { accommodationApi } from '@/api/accommodation.js'
import { reviewApi } from '@/api/review.js'

const route = useRoute()
const router = useRouter()
const accommodationId = Number(route.params.id)

const accommodation = ref(null)
const rooms = ref([])
const reviews = ref([])
const rating = ref({ averageRating: null, reviewCount: 0 })
const loading = ref(true)
const reviewPage = ref(0)
const hasMoreReviews = ref(false)

const TYPE_MAP = {
  HOTEL: '호텔', MOTEL: '모텔', PENSION: '펜션',
  GUESTHOUSE: '게스트하우스', RESORT: '리조트'
}
function typeLabel(t) { return TYPE_MAP[t] ?? t }
function formatDate(dt) { return dt ? dt.slice(0, 10) : '' }

async function loadReviews(page = 0) {
  const res = await reviewApi.getByAccommodation(accommodationId, { page, size: 5 })
  const data = res.data
  if (page === 0) reviews.value = data.content ?? []
  else reviews.value.push(...(data.content ?? []))
  hasMoreReviews.value = !(data.last ?? true)
  reviewPage.value = page
}

async function loadMoreReviews() {
  await loadReviews(reviewPage.value + 1)
}

function goReserve(room) {
  router.push({
    path: '/reservations/new',
    query: { roomId: room.id, roomName: room.name, basePrice: room.basePrice, accommodationName: accommodation.value?.name }
  })
}

onMounted(async () => {
  try {
    const [accRes, roomRes, ratingRes] = await Promise.all([
      accommodationApi.getById(accommodationId),
      accommodationApi.getRooms(accommodationId),
      reviewApi.getRating(accommodationId)
    ])
    accommodation.value = accRes.data
    rooms.value = Array.isArray(roomRes.data) ? roomRes.data : (roomRes.data.content ?? [])
    rating.value = ratingRes.data
    await loadReviews()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.section-title { font-size: 1.125rem; font-weight: 600; margin-bottom: .75rem; }
.room-price { font-weight: 700; color: var(--primary); font-size: 1.1rem; margin-top: .5rem; }
.ml-2 { margin-left: .5rem; }

.review-item { padding: 1rem 0; border-bottom: 1px solid var(--border); }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; align-items: center; gap: .75rem; margin-bottom: .375rem; }
.review-author { font-weight: 600; font-size: .875rem; }
.review-rating { color: #f59e0b; font-weight: 600; }
.review-content { font-size: .9rem; color: var(--text); line-height: 1.6; }
</style>

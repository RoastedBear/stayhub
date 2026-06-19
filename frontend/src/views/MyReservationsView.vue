<template>
  <div class="container" style="max-width:800px;">
    <div class="page-header flex justify-between items-center">
      <h1 class="page-title">내 예약 목록</h1>
    </div>

    <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

    <div v-else-if="reservations.length === 0" class="text-center text-muted" style="padding:3rem 0;">
      예약 내역이 없습니다.
    </div>

    <div v-else class="reservation-list">
      <div v-for="res in reservations" :key="res.id" class="card reservation-card">
        <div class="reservation-header">
          <div>
            <h3 class="reservation-name">{{ res.accommodationName }}</h3>
            <p class="text-muted" style="font-size:.875rem;">{{ res.roomName }}</p>
          </div>
          <span :class="statusBadge(res.status)">{{ statusLabel(res.status) }}</span>
        </div>

        <div class="reservation-info">
          <span>📅 {{ res.checkInDate }} ~ {{ res.checkOutDate }}</span>
          <span>👥 {{ res.guestCount }}명</span>
          <span class="reservation-price">{{ res.totalPrice?.toLocaleString() }}원</span>
        </div>

        <div v-if="res.status === 'CONFIRMED'" class="flex gap-2 mt-4">
          <button class="btn btn-danger btn-sm" @click="handleCancel(res.id)">
            예약 취소
          </button>
          <RouterLink :to="`/accommodations/${getAccommodationId(res)}`" class="btn btn-outline btn-sm">
            리뷰 작성
          </RouterLink>
        </div>

        <div v-if="res.status === 'COMPLETED'" class="mt-4">
          <!-- 리뷰 작성 폼 인라인 -->
          <div v-if="writingReviewFor === res.id" class="review-form">
            <div v-if="reviewError" class="alert alert-error">{{ reviewError }}</div>
            <div class="form-group mb-4">
              <label class="form-label">별점</label>
              <select v-model.number="reviewForm.rating">
                <option v-for="n in 5" :key="n" :value="n">★ {{ n }}</option>
              </select>
            </div>
            <div class="form-group mb-4">
              <label class="form-label">리뷰 내용 (10~1000자)</label>
              <textarea v-model="reviewForm.content" rows="4" minlength="10" maxlength="1000" />
            </div>
            <div class="flex gap-2">
              <button class="btn btn-primary btn-sm" @click="submitReview(res.id)">등록</button>
              <button class="btn btn-outline btn-sm" @click="writingReviewFor = null">취소</button>
            </div>
          </div>
          <button v-else class="btn btn-outline btn-sm" @click="openReviewForm(res.id)">
            리뷰 작성
          </button>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalPages > 1" class="pagination mt-6">
      <button class="btn btn-outline" :disabled="page === 0" @click="loadPage(page - 1)">이전</button>
      <span class="text-muted">{{ page + 1 }} / {{ totalPages }}</span>
      <button class="btn btn-outline" :disabled="page >= totalPages - 1" @click="loadPage(page + 1)">다음</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reservationApi } from '@/api/reservation.js'
import { reviewApi } from '@/api/review.js'

const reservations = ref([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(1)
const writingReviewFor = ref(null)
const reviewForm = ref({ rating: 5, content: '' })
const reviewError = ref('')

const STATUS_LABEL = {
  PENDING: '대기중', CONFIRMED: '확정', COMPLETED: '완료', CANCELLED: '취소'
}
const STATUS_BADGE = {
  PENDING: 'badge badge-yellow', CONFIRMED: 'badge badge-green',
  COMPLETED: 'badge badge-blue', CANCELLED: 'badge badge-gray'
}
function statusLabel(s) { return STATUS_LABEL[s] ?? s }
function statusBadge(s) { return STATUS_BADGE[s] ?? 'badge badge-gray' }

function getAccommodationId(res) {
  return res.accommodationId ?? ''
}

async function loadPage(p = 0) {
  loading.value = true
  page.value = p
  try {
    const res = await reservationApi.getMyReservations({ page: p, size: 10 })
    reservations.value = res.data.content ?? res.data
    totalPages.value = res.data.totalPages ?? 1
  } finally {
    loading.value = false
  }
}

async function handleCancel(reservationId) {
  if (!confirm('예약을 취소하시겠습니까?')) return
  try {
    await reservationApi.cancel(reservationId)
    await loadPage(page.value)
  } catch (e) {
    alert(e.response?.data?.message ?? '취소에 실패했습니다.')
  }
}

function openReviewForm(id) {
  writingReviewFor.value = id
  reviewForm.value = { rating: 5, content: '' }
  reviewError.value = ''
}

async function submitReview(reservationId) {
  reviewError.value = ''
  if (reviewForm.value.content.length < 10) {
    reviewError.value = '리뷰는 10자 이상 입력해주세요.'
    return
  }
  try {
    await reviewApi.create({ reservationId, ...reviewForm.value })
    writingReviewFor.value = null
    alert('리뷰가 등록되었습니다.')
  } catch (e) {
    reviewError.value = e.response?.data?.message ?? '리뷰 등록에 실패했습니다.'
  }
}

onMounted(() => loadPage())
</script>

<style scoped>
.reservation-list { display: flex; flex-direction: column; gap: 1rem; }
.reservation-card {}
.reservation-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: .75rem; }
.reservation-name { font-weight: 600; font-size: 1rem; }
.reservation-info { display: flex; align-items: center; gap: 1.5rem; flex-wrap: wrap; font-size: .875rem; color: var(--text-muted); }
.reservation-price { font-weight: 700; color: var(--primary); font-size: 1rem; }
.btn-sm { padding: .375rem .875rem; font-size: .875rem; }
.review-form { background: var(--bg); border-radius: var(--radius); padding: 1rem; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 1rem; }
</style>

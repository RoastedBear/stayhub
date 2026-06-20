<template>
  <div class="container" style="max-width:560px;">
    <div class="card">
      <div class="page-header">
        <h1 class="page-title">예약 및 결제</h1>
        <p class="text-muted">{{ route.query.accommodationName }} - {{ route.query.roomName }}</p>
      </div>

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <form @submit.prevent="handleReserve">
        <div class="form-group mb-4">
          <label class="form-label">체크인 날짜</label>
          <input v-model="form.checkInDate" type="date" :min="today" required />
        </div>
        <div class="form-group mb-4">
          <label class="form-label">체크아웃 날짜</label>
          <input v-model="form.checkOutDate" type="date" :min="form.checkInDate || today" required />
        </div>
        <div class="form-group mb-4">
          <label class="form-label">인원 수</label>
          <input v-model.number="form.guestCount" type="number" min="1" required />
        </div>

        <!-- 요금 계산 -->
        <div v-if="nights > 0" class="price-summary card" style="background:var(--bg); margin-bottom:1rem;">
          <div class="flex justify-between">
            <span>{{ basePrice?.toLocaleString() }}원 × {{ nights }}박</span>
            <strong>{{ totalPrice.toLocaleString() }}원</strong>
          </div>
        </div>

        <button type="submit" class="btn btn-primary w-full" :disabled="loading || nights <= 0">
          {{ loading ? '처리 중...' : `${totalPrice.toLocaleString()}원 결제하기` }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reservationApi } from '@/api/reservation.js'
import { paymentApi } from '@/api/payment.js'

const route = useRoute()
const router = useRouter()
const today = new Date().toLocaleDateString('en-CA')

const roomId = Number(route.query.roomId)
const basePrice = Number(route.query.basePrice) || 0

const form = ref({ checkInDate: '', checkOutDate: '', guestCount: 2 })
const error = ref('')
const loading = ref(false)

const nights = computed(() => {
  if (!form.value.checkInDate || !form.value.checkOutDate) return 0
  const diff = new Date(form.value.checkOutDate) - new Date(form.value.checkInDate)
  return Math.max(0, Math.floor(diff / 86400000))
})

const totalPrice = computed(() => basePrice * nights.value)

async function handleReserve() {
  if (nights.value <= 0) { error.value = '날짜를 올바르게 선택해주세요.'; return }
  error.value = ''
  loading.value = true

  let reservationId = null
  try {
    // 1. 예약 생성
    const resRes = await reservationApi.create({
      roomId,
      checkInDate: form.value.checkInDate,
      checkOutDate: form.value.checkOutDate,
      guestCount: form.value.guestCount
    })
    reservationId = resRes.data.id

    // 2. 결제 준비 (orderId, amount 발급)
    const prepareRes = await paymentApi.prepare(reservationId)
    const { orderId, orderName, amount } = prepareRes.data

    // 3. Toss 결제 위젯 호출 (성공 시 successUrl로 리다이렉트)
    const toss = window.TossPayments(import.meta.env.VITE_TOSS_CLIENT_KEY || 'test_ck_dummy')
    await toss.requestPayment('카드', {
      amount,
      orderId,
      orderName,
      successUrl: `${window.location.origin}/payment/success`,
      failUrl: `${window.location.origin}/payment/fail`
    })
  } catch (e) {
    // 결제 진행 중 에러 발생 시 생성된 예약을 자동 취소
    if (reservationId) {
      await reservationApi.cancel(reservationId).catch(() => {})
    }
    error.value = e.code
      ? (e.message || '결제가 취소되었습니다.')
      : (e.response?.data?.message ?? '예약 처리 중 오류가 발생했습니다.')
    loading.value = false
  }
}
</script>

<style scoped>
.price-summary { padding: .875rem 1rem; }
</style>

<template>
  <div class="container text-center" style="max-width:480px; padding-top:4rem;">
    <div class="card">
      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <template v-else-if="confirmed">
        <div style="font-size:3rem; margin-bottom:1rem;">✅</div>
        <h1 class="page-title mb-4">결제가 완료되었습니다!</h1>
        <div class="price-summary mb-4" style="text-align:left;">
          <div class="summary-row">
            <span>결제 금액</span>
            <strong>{{ Number(route.query.amount).toLocaleString() }}원</strong>
          </div>
          <div class="summary-row">
            <span>결제 수단</span>
            <span>{{ confirmResult?.method ?? '-' }}</span>
          </div>
          <div class="summary-row">
            <span>주문번호</span>
            <span style="font-size:.8rem;">{{ route.query.orderId }}</span>
          </div>
        </div>
        <div class="flex gap-2">
          <RouterLink to="/my/reservations" class="btn btn-primary" style="flex:1;">내 예약 보기</RouterLink>
          <RouterLink to="/" class="btn btn-outline" style="flex:1;">홈으로</RouterLink>
        </div>
      </template>

      <template v-else-if="error">
        <div style="font-size:3rem; margin-bottom:1rem;">❌</div>
        <h1 class="page-title mb-4">결제 승인 실패</h1>
        <p class="text-muted mb-4">{{ error }}</p>
        <RouterLink to="/" class="btn btn-outline">홈으로</RouterLink>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { paymentApi } from '@/api/payment.js'

const route = useRoute()
const loading = ref(true)
const confirmed = ref(false)
const confirmResult = ref(null)
const error = ref('')

onMounted(async () => {
  const { paymentKey, orderId, amount } = route.query
  if (!paymentKey || !orderId || !amount) {
    error.value = '잘못된 접근입니다.'
    loading.value = false
    return
  }
  try {
    const res = await paymentApi.confirm({
      paymentKey,
      orderId,
      amount: Number(amount)
    })
    confirmResult.value = res.data
    confirmed.value = true
  } catch (e) {
    error.value = e.response?.data?.message ?? '결제 승인에 실패했습니다.'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.price-summary { background: var(--bg); border-radius: var(--radius); padding: 1rem; }
.summary-row {
  display: flex; justify-content: space-between;
  padding: .375rem 0;
  border-bottom: 1px solid var(--border);
  font-size: .9rem;
}
.summary-row:last-child { border-bottom: none; }
</style>

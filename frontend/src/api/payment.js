import api from './index.js'

export const paymentApi = {
  // 결제 준비 (orderId, amount 발급)
  prepare: (reservationId) => api.post(`/payments/prepare/${reservationId}`),

  // Toss 결제 승인 (Toss 콜백 후 호출)
  confirm: (data) => api.post('/payments/confirm', data),

  // 결제 취소
  cancel: (reservationId, data) => api.post(`/payments/${reservationId}/cancel`, data),

  // 결제 조회
  getByReservation: (reservationId) => api.get(`/payments/${reservationId}`)
}

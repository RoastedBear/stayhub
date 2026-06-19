import api from './index.js'

export const reservationApi = {
  create: (data) => api.post('/reservations', data),
  getById: (id) => api.get(`/reservations/${id}`),
  getMyReservations: (params) => api.get('/reservations/my', { params }),
  cancel: (id) => api.patch(`/reservations/${id}/cancel`)
}

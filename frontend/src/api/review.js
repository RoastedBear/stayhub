import api from './index.js'

export const reviewApi = {
  create: (data) => api.post('/reviews', data),
  update: (reviewId, data) => api.put(`/reviews/${reviewId}`, data),
  delete: (reviewId) => api.delete(`/reviews/${reviewId}`),
  getByAccommodation: (accommodationId, params) =>
    api.get(`/reviews/accommodations/${accommodationId}`, { params }),
  getRating: (accommodationId) =>
    api.get(`/reviews/accommodations/${accommodationId}/rating`)
}

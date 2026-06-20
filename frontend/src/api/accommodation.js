import api from './index.js'

export const accommodationApi = {
  // 공개 API
  getById: (id) => api.get(`/accommodations/${id}`),
  getRooms: (id) => api.get(`/accommodations/${id}/rooms`),
  searchRooms: (params) => api.get('/rooms/search', { params }),

  // 호스트 API (인증 필요)
  getMyAccommodations: () => api.get('/accommodations/my'),
  create: (data) => api.post('/accommodations', data),
  update: (id, data) => api.put(`/accommodations/${id}`, data),
  delete: (id) => api.delete(`/accommodations/${id}`),

  // 객실 관리
  createRoom: (accommodationId, data) => api.post(`/accommodations/${accommodationId}/rooms`, data),
  updateRoom: (roomId, data) => api.put(`/rooms/${roomId}`, data),
  deleteRoom: (roomId) => api.delete(`/rooms/${roomId}`),

  // 이미지 업로드
  uploadAccommodationImages: (accommodationId, files) => {
    const form = new FormData()
    files.forEach(f => form.append('files', f))
    return api.post(`/accommodations/${accommodationId}/images`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  deleteAccommodationImage: (accommodationId, imageId) =>
    api.delete(`/accommodations/${accommodationId}/images/${imageId}`),

  uploadRoomImages: (roomId, files) => {
    const form = new FormData()
    files.forEach(f => form.append('files', f))
    return api.post(`/rooms/${roomId}/images`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

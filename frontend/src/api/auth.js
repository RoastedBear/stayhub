import api from './index.js'

export const authApi = {
  signup: (data) => api.post('/auth/signup', data),
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout', {
    refreshToken: localStorage.getItem('refreshToken')
  })
}

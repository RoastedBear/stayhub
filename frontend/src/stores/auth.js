import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth.js'

// JWT 페이로드 디코딩 (검증 없이 claims만 파싱)
function decodeJwt(token) {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const member = ref(null)

  // 토큰에서 사용자 정보 복원
  function restoreFromToken() {
    const token = accessToken.value
    if (!token) return
    const claims = decodeJwt(token)
    if (!claims) return
    // claims: sub(memberId), email, name, roles(comma-separated)
    member.value = {
      id: claims.sub,
      email: claims.email,
      name: claims.name,
      roles: claims.roles ? claims.roles.split(',') : []
    }
  }

  restoreFromToken()

  const isLoggedIn = computed(() => !!accessToken.value)
  const isHost = computed(() => member.value?.roles?.includes('HOST') ?? false)
  const memberName = computed(() => member.value?.name ?? '')

  async function login(email, password) {
    const res = await authApi.login({ email, password })
    const { accessToken: at, refreshToken: rt } = res.data
    accessToken.value = at
    localStorage.setItem('accessToken', at)
    localStorage.setItem('refreshToken', rt)
    restoreFromToken()
  }

  async function signup(data) {
    await authApi.signup(data)
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 실패해도 로컬 상태는 초기화
    } finally {
      accessToken.value = null
      member.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
    }
  }

  return { accessToken, member, isLoggedIn, isHost, memberName, login, signup, logout }
})

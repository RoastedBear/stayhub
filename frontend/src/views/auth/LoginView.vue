<template>
  <div class="container" style="max-width:420px; padding-top:4rem;">
    <div class="card">
      <h1 class="page-title text-center mb-4">로그인</h1>

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <form @submit.prevent="handleLogin">
        <div class="form-group mb-4">
          <label class="form-label">이메일</label>
          <input v-model="form.email" type="email" placeholder="이메일 입력" required />
        </div>
        <div class="form-group mb-4">
          <label class="form-label">비밀번호</label>
          <input v-model="form.password" type="password" placeholder="비밀번호 입력" required />
        </div>
        <button type="submit" class="btn btn-primary w-full" :disabled="loading">
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <p class="text-center text-muted mt-4">
        계정이 없으신가요?
        <RouterLink to="/signup" style="color:var(--primary)">회원가입</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = ref({ email: '', password: '' })
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(form.value.email, form.value.password)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    error.value = e.response?.data?.message ?? '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container" style="max-width:480px; padding-top:4rem;">
    <div class="card">
      <h1 class="page-title text-center mb-4">회원가입</h1>

      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert alert-success">회원가입 완료! 로그인해주세요.</div>

      <form v-if="!success" @submit.prevent="handleSignup">
        <div class="form-group mb-4">
          <label class="form-label">이름</label>
          <input v-model="form.name" type="text" placeholder="이름 입력" required />
        </div>
        <div class="form-group mb-4">
          <label class="form-label">이메일</label>
          <input v-model="form.email" type="email" placeholder="이메일 입력" required />
        </div>
        <div class="form-group mb-4">
          <label class="form-label">비밀번호</label>
          <input v-model="form.password" type="password" placeholder="8자 이상" required minlength="8" />
        </div>
        <div class="form-group mb-4">
          <label class="form-label">전화번호</label>
          <input v-model="form.phoneNumber" type="tel" placeholder="010-0000-0000" />
        </div>
        <button type="submit" class="btn btn-primary w-full" :disabled="loading">
          {{ loading ? '처리 중...' : '회원가입' }}
        </button>
      </form>

      <p class="text-center text-muted mt-4">
        이미 계정이 있으신가요?
        <RouterLink to="/login" style="color:var(--primary)">로그인</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth.js'

const auth = useAuthStore()

const form = ref({ name: '', email: '', password: '', phoneNumber: '' })
const error = ref('')
const success = ref(false)
const loading = ref(false)

async function handleSignup() {
  error.value = ''
  loading.value = true
  try {
    await auth.signup(form.value)
    success.value = true
  } catch (e) {
    error.value = e.response?.data?.message ?? '회원가입에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

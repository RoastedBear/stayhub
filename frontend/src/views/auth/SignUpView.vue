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
          <div style="display:flex; gap:8px;">
            <input
              v-model="form.email"
              type="email"
              placeholder="이메일 입력"
              required
              style="flex:1;"
              @input="resetEmailCheck"
            />
            <button
              type="button"
              class="btn btn-secondary"
              style="white-space:nowrap;"
              :disabled="!form.email || checkingEmail"
              @click="checkEmail"
            >
              {{ checkingEmail ? '확인 중...' : '중복확인' }}
            </button>
          </div>
          <p v-if="emailMessage" :style="{ color: emailAvailable ? '#22c55e' : '#ef4444', fontSize: '0.85rem', marginTop: '4px' }">
            {{ emailMessage }}
          </p>
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
import axios from 'axios'

const auth = useAuthStore()

const form = ref({ name: '', email: '', password: '', phoneNumber: '' })
const error = ref('')
const success = ref(false)
const loading = ref(false)

const emailChecked = ref(false)
const emailAvailable = ref(false)
const emailMessage = ref('')
const checkingEmail = ref(false)

function resetEmailCheck() {
  emailChecked.value = false
  emailAvailable.value = false
  emailMessage.value = ''
}

async function checkEmail() {
  if (!form.value.email) return
  checkingEmail.value = true
  try {
    const res = await axios.get('/api/auth/check-email', { params: { email: form.value.email } })
    emailAvailable.value = res.data.available
    emailChecked.value = true
    emailMessage.value = res.data.available ? '사용 가능한 이메일입니다.' : '이미 사용 중인 이메일입니다.'
  } catch {
    emailMessage.value = '이메일 확인 중 오류가 발생했습니다.'
    emailChecked.value = false
  } finally {
    checkingEmail.value = false
  }
}

async function handleSignup() {
  error.value = ''

  if (!emailChecked.value) {
    error.value = '이메일 중복확인을 해주세요.'
    return
  }
  if (!emailAvailable.value) {
    error.value = '이미 사용 중인 이메일입니다. 다른 이메일을 입력해주세요.'
    return
  }

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

<template>
  <header class="navbar">
    <div class="container navbar-inner">
      <RouterLink to="/" class="navbar-brand">StayHub</RouterLink>

      <nav class="navbar-nav">
        <RouterLink to="/">홈</RouterLink>
        <template v-if="auth.isLoggedIn">
          <RouterLink to="/my/reservations">내 예약</RouterLink>
          <RouterLink to="/host/dashboard">호스트</RouterLink>
          <button class="btn btn-outline btn-sm" @click="handleLogout">로그아웃</button>
          <span class="nav-user">{{ auth.memberName }}</span>
        </template>
        <template v-else>
          <RouterLink to="/login" class="btn btn-primary btn-sm">로그인</RouterLink>
          <RouterLink to="/signup" class="btn btn-outline btn-sm">회원가입</RouterLink>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth.js'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  position: sticky; top: 0; z-index: 100;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  box-shadow: var(--shadow);
}
.navbar-inner {
  display: flex; align-items: center; justify-content: space-between;
  height: 3.5rem;
}
.navbar-brand {
  font-size: 1.25rem; font-weight: 700; color: var(--primary);
}
.navbar-nav {
  display: flex; align-items: center; gap: 1rem;
}
.navbar-nav a { font-size: .9rem; color: var(--text); }
.navbar-nav a:hover { color: var(--primary); }
.btn-sm { padding: .375rem .875rem; font-size: .875rem; }
.nav-user { font-size: .875rem; color: var(--text-muted); }
</style>

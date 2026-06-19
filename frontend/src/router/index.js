import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'

const routes = [
  { path: '/', component: () => import('@/views/HomeView.vue') },
  { path: '/login', component: () => import('@/views/auth/LoginView.vue') },
  { path: '/signup', component: () => import('@/views/auth/SignUpView.vue') },
  {
    path: '/accommodations/:id',
    component: () => import('@/views/AccommodationDetailView.vue')
  },
  {
    path: '/reservations/new',
    component: () => import('@/views/ReservationView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/payment/success',
    component: () => import('@/views/payment/PaymentSuccessView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/payment/fail',
    component: () => import('@/views/payment/PaymentFailView.vue')
  },
  {
    path: '/my/reservations',
    component: () => import('@/views/MyReservationsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/host/dashboard',
    component: () => import('@/views/host/HostDashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/host/accommodations/new',
    component: () => import('@/views/host/AccommodationFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/host/accommodations/:id/edit',
    component: () => import('@/views/host/AccommodationFormView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth) {
    const auth = useAuthStore()
    if (!auth.isLoggedIn) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
})

export default router

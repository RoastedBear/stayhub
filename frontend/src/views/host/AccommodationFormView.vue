<template>
  <div class="container" style="max-width:640px;">
    <div class="card">
      <div class="page-header">
        <h1 class="page-title">{{ isEdit ? '숙소 수정' : '숙소 등록' }}</h1>
      </div>

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <form @submit.prevent="handleSubmit">
        <div class="form-group mb-4">
          <label class="form-label">숙소명 *</label>
          <input v-model="form.name" type="text" placeholder="숙소 이름" required maxlength="100" />
        </div>

        <div class="form-group mb-4">
          <label class="form-label">숙소 유형 *</label>
          <select v-model="form.type" required>
            <option value="">선택해주세요</option>
            <option value="HOTEL">호텔</option>
            <option value="MOTEL">모텔</option>
            <option value="PENSION">펜션</option>
            <option value="GUESTHOUSE">게스트하우스</option>
            <option value="RESORT">리조트</option>
          </select>
        </div>

        <div class="form-group mb-4">
          <label class="form-label">숙소 소개 *</label>
          <textarea v-model="form.description" rows="5" placeholder="숙소를 소개해주세요" required maxlength="2000" />
        </div>

        <div class="addr-row mb-4">
          <div class="form-group">
            <label class="form-label">시/도 *</label>
            <input v-model="form.sido" type="text" placeholder="서울특별시" required />
          </div>
          <div class="form-group">
            <label class="form-label">시/군/구 *</label>
            <input v-model="form.sigungu" type="text" placeholder="강남구" required />
          </div>
        </div>

        <div class="form-group mb-4">
          <label class="form-label">상세 주소 *</label>
          <input v-model="form.address" type="text" placeholder="상세 주소 입력" required />
        </div>

        <div class="addr-row mb-4">
          <div class="form-group">
            <label class="form-label">위도</label>
            <input v-model.number="form.latitude" type="number" step="any" placeholder="37.5665" />
          </div>
          <div class="form-group">
            <label class="form-label">경도</label>
            <input v-model.number="form.longitude" type="number" step="any" placeholder="126.9780" />
          </div>
        </div>

        <div class="flex gap-2">
          <button type="submit" class="btn btn-primary" style="flex:1;" :disabled="loading">
            {{ loading ? '처리 중...' : (isEdit ? '수정 완료' : '등록하기') }}
          </button>
          <button type="button" class="btn btn-outline" style="flex:1;" @click="$router.back()">
            취소
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { accommodationApi } from '@/api/accommodation.js'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const accommodationId = computed(() => Number(route.params.id) || null)

const form = ref({
  name: '', type: '', description: '',
  sido: '', sigungu: '', address: '',
  latitude: null, longitude: null
})
const error = ref('')
const loading = ref(false)

// 수정 모드: 기존 데이터 로드
onMounted(async () => {
  if (isEdit.value) {
    try {
      const res = await accommodationApi.getById(accommodationId.value)
      const d = res.data
      form.value = {
        name: d.name,
        type: d.type,
        description: d.description,
        sido: d.sido,
        sigungu: d.sigungu,
        address: d.address,
        latitude: d.latitude,
        longitude: d.longitude
      }
    } catch (e) {
      error.value = '숙소 정보를 불러오지 못했습니다.'
    }
  }
})

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    const payload = { ...form.value }
    if (!payload.latitude) delete payload.latitude
    if (!payload.longitude) delete payload.longitude

    if (isEdit.value) {
      await accommodationApi.update(accommodationId.value, payload)
    } else {
      await accommodationApi.create(payload)
    }
    router.push('/host/dashboard')
  } catch (e) {
    error.value = e.response?.data?.message ?? '처리 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.addr-row { display: grid; grid-template-columns: 1fr 1fr; gap: .75rem; }
</style>

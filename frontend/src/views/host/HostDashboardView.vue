<template>
  <div class="container" style="max-width:900px;">
    <div class="page-header flex justify-between items-center">
      <h1 class="page-title">호스트 대시보드</h1>
      <RouterLink to="/host/accommodations/new" class="btn btn-primary">+ 숙소 등록</RouterLink>
    </div>

    <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

    <div v-else-if="accommodations.length === 0" class="text-center text-muted" style="padding:3rem 0;">
      등록한 숙소가 없습니다.
      <br /><br />
      <RouterLink to="/host/accommodations/new" class="btn btn-primary">첫 숙소 등록하기</RouterLink>
    </div>

    <div v-else class="accommodation-list">
      <div v-for="acc in accommodations" :key="acc.id" class="card accommodation-item">
        <!-- 숙소 헤더 -->
        <div class="acc-header">
          <div>
            <h3 class="acc-name">{{ acc.name }}</h3>
            <p class="text-muted" style="font-size:.875rem;">{{ acc.sido }} {{ acc.sigungu }}</p>
          </div>
          <div class="flex gap-2 items-center">
            <span :class="accStatusBadge(acc.status)">{{ accStatusLabel(acc.status) }}</span>
            <button class="btn btn-sm" :class="selectedAccId === acc.id ? 'btn-primary' : 'btn-outline'" @click="toggleRoomPanel(acc.id)">
              객실 관리
            </button>
            <RouterLink :to="`/host/accommodations/${acc.id}/edit`" class="btn btn-outline btn-sm">수정</RouterLink>
            <button class="btn btn-danger btn-sm" @click="handleDelete(acc.id)">삭제</button>
          </div>
        </div>

        <p class="text-muted" style="font-size:.875rem; margin-top:.5rem;">
          {{ acc.description?.slice(0, 80) }}{{ acc.description?.length > 80 ? '...' : '' }}
        </p>

        <RouterLink :to="`/accommodations/${acc.id}`" class="text-muted" style="font-size:.8rem; margin-top:.5rem; display:block;">
          숙소 상세 보기 →
        </RouterLink>

        <!-- 객실 관리 패널 -->
        <div v-if="selectedAccId === acc.id" class="room-panel">
          <div class="room-panel-header">
            <h4>객실 목록</h4>
            <button class="btn btn-primary btn-sm" @click="openAddRoom(acc.id)">+ 객실 추가</button>
          </div>

          <!-- 객실 추가/수정 폼 -->
          <div v-if="showRoomForm && formAccId === acc.id" class="room-form">
            <h5>{{ editingRoomId ? '객실 수정' : '새 객실 추가' }}</h5>
            <div class="room-form-grid">
              <div class="form-group">
                <label class="form-label">객실명 *</label>
                <input v-model="roomForm.name" type="text" placeholder="디럭스 더블룸" />
              </div>
              <div class="form-group">
                <label class="form-label">최대 인원 *</label>
                <input v-model.number="roomForm.maxOccupancy" type="number" min="1" max="20" placeholder="2" />
              </div>
              <div class="form-group">
                <label class="form-label">1박 가격 (원) *</label>
                <input v-model.number="roomForm.basePrice" type="number" min="0" step="1000" placeholder="100000" />
              </div>
              <div class="form-group" style="grid-column: 1 / -1;">
                <label class="form-label">설명</label>
                <textarea v-model="roomForm.description" rows="2" placeholder="객실 설명..." maxlength="1000" />
              </div>
            </div>
            <div class="flex gap-2" style="margin-top:.75rem;">
              <button class="btn btn-primary btn-sm" :disabled="roomSubmitting" @click="submitRoomForm(acc.id)">
                {{ roomSubmitting ? '처리 중...' : (editingRoomId ? '수정 완료' : '추가') }}
              </button>
              <button class="btn btn-outline btn-sm" @click="closeRoomForm">취소</button>
            </div>
          </div>

          <!-- 객실 목록 -->
          <div v-if="roomLoading" class="text-muted" style="padding:.75rem 0; font-size:.875rem;">로딩 중...</div>
          <div v-else-if="rooms.length === 0" class="text-muted" style="padding:.75rem 0; font-size:.875rem;">
            등록된 객실이 없습니다.
          </div>
          <div v-else class="room-list">
            <div v-for="room in rooms" :key="room.id" class="room-item">
              <div class="room-item-info">
                <span class="room-item-name">{{ room.name }}</span>
                <span class="text-muted" style="font-size:.8rem;">최대 {{ room.maxOccupancy }}명 · {{ room.basePrice?.toLocaleString() }}원/박</span>
                <span v-if="room.description" class="text-muted" style="font-size:.8rem;">
                  {{ room.description?.slice(0, 60) }}{{ room.description?.length > 60 ? '...' : '' }}
                </span>
              </div>
              <div class="flex gap-2">
                <button class="btn btn-outline btn-sm" @click="openEditRoom(room)">수정</button>
                <button class="btn btn-danger btn-sm" @click="handleDeleteRoom(room.id)">삭제</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { accommodationApi } from '@/api/accommodation.js'

const accommodations = ref([])
const loading = ref(true)

const selectedAccId = ref(null)
const rooms = ref([])
const roomLoading = ref(false)
const showRoomForm = ref(false)
const formAccId = ref(null)
const editingRoomId = ref(null)
const roomSubmitting = ref(false)
const roomForm = ref({ name: '', description: '', maxOccupancy: 2, basePrice: 0 })

const STATUS_LABEL = { ACTIVE: '운영중', INACTIVE: '비활성', DELETED: '삭제됨' }
const STATUS_BADGE = {
  ACTIVE: 'badge badge-green', INACTIVE: 'badge badge-yellow', DELETED: 'badge badge-red'
}
function accStatusLabel(s) { return STATUS_LABEL[s] ?? s }
function accStatusBadge(s) { return STATUS_BADGE[s] ?? 'badge badge-gray' }

async function loadAccommodations() {
  loading.value = true
  try {
    const res = await accommodationApi.getMyAccommodations()
    accommodations.value = res.data.content ?? res.data ?? []
  } finally {
    loading.value = false
  }
}

async function toggleRoomPanel(accId) {
  if (selectedAccId.value === accId) {
    selectedAccId.value = null
    closeRoomForm()
    return
  }
  selectedAccId.value = accId
  closeRoomForm()
  await loadRooms(accId)
}

async function loadRooms(accId) {
  roomLoading.value = true
  try {
    const res = await accommodationApi.getRooms(accId)
    rooms.value = res.data.content ?? res.data ?? []
  } finally {
    roomLoading.value = false
  }
}

function openAddRoom(accId) {
  roomForm.value = { name: '', description: '', maxOccupancy: 2, basePrice: 0 }
  editingRoomId.value = null
  formAccId.value = accId
  showRoomForm.value = true
}

function openEditRoom(room) {
  roomForm.value = {
    name: room.name,
    description: room.description ?? '',
    maxOccupancy: room.maxOccupancy,
    basePrice: room.basePrice
  }
  editingRoomId.value = room.id
  formAccId.value = selectedAccId.value
  showRoomForm.value = true
}

function closeRoomForm() {
  showRoomForm.value = false
  editingRoomId.value = null
  formAccId.value = null
}

async function submitRoomForm(accId) {
  if (!roomForm.value.name || !roomForm.value.maxOccupancy || !roomForm.value.basePrice) {
    alert('객실명, 최대 인원, 가격은 필수입니다.')
    return
  }
  roomSubmitting.value = true
  try {
    if (editingRoomId.value) {
      await accommodationApi.updateRoom(editingRoomId.value, roomForm.value)
    } else {
      await accommodationApi.createRoom(accId, roomForm.value)
    }
    closeRoomForm()
    await loadRooms(accId)
  } catch (e) {
    alert(e.response?.data?.message ?? '처리 중 오류가 발생했습니다.')
  } finally {
    roomSubmitting.value = false
  }
}

async function handleDeleteRoom(roomId) {
  if (!confirm('객실을 삭제하시겠습니까?')) return
  try {
    await accommodationApi.deleteRoom(roomId)
    await loadRooms(selectedAccId.value)
  } catch (e) {
    alert(e.response?.data?.message ?? '삭제에 실패했습니다.')
  }
}

async function handleDelete(id) {
  if (!confirm('숙소를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return
  try {
    await accommodationApi.delete(id)
    if (selectedAccId.value === id) selectedAccId.value = null
    await loadAccommodations()
  } catch (e) {
    alert(e.response?.data?.message ?? '삭제에 실패했습니다.')
  }
}

onMounted(loadAccommodations)
</script>

<style scoped>
.accommodation-list { display: flex; flex-direction: column; gap: 1rem; }
.acc-header { display: flex; justify-content: space-between; align-items: flex-start; }
.acc-name { font-weight: 600; font-size: 1.05rem; }
.btn-sm { padding: .375rem .875rem; font-size: .875rem; }

.room-panel {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border);
}
.room-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: .75rem;
}
.room-panel-header h4 { font-weight: 600; font-size: .95rem; }

.room-form {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 1rem;
  margin-bottom: 1rem;
}
.room-form h5 { font-weight: 600; font-size: .875rem; margin-bottom: .75rem; }
.room-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: .75rem;
}

.room-list { display: flex; flex-direction: column; gap: .5rem; }
.room-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: .625rem .75rem;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
.room-item-info { display: flex; flex-direction: column; gap: .2rem; }
.room-item-name { font-weight: 600; font-size: .9rem; }
</style>

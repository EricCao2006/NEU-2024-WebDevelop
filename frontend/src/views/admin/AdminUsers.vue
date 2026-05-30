<template>
  <div class="admin-users">
    <!-- 页面头部 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon"><</span>
        <span class="back-text">返回</span>
      </button>
      <h2 class="page-title">👥 用户管理</h2>
    </div>

    <!-- 角色筛选 -->
    <div class="filter-tabs">
      <button
          v-for="filter in filters"
          :key="filter.value"
          :class="['filter-btn', { active: currentFilter === filter.value }]"
          @click="currentFilter = filter.value"
      >
        {{ filter.label }}
      </button>
    </div>

    <!-- 用户列表 -->
    <div class="users-list">
      <div v-for="user in filteredUsers" :key="user.id" class="user-card">
        <div class="user-avatar">
          {{ user.username?.charAt(0) || '👤' }}
        </div>
        <div class="user-info">
          <div class="user-name">{{ user.username }}</div>
          <div class="user-phone">{{ user.phone }}</div>
          <div class="user-address">{{ user.address || '未设置' }}</div>
        </div>
        <div class="user-status">
          <span :class="['status-badge', user.status === 'active' ? 'status-active' : 'status-disabled']">
            {{ user.status === 'active' ? '正常' : '禁用' }}
          </span>
        </div>
        <div class="user-actions">
          <select v-model="user.role" @change="updateRole(user.id, user.role)" class="role-select">
            <option value="customer">顾客</option>
            <option value="merchant">商家</option>
            <option value="admin">管理员</option>
          </select>
          <button
              :class="['btn-status', user.status === 'active' ? 'btn-disable' : 'btn-enable']"
              @click="toggleStatus(user.id, user.status)"
          >
            {{ user.status === 'active' ? '禁用' : '启用' }}
          </button>
          <button class="btn-reset" @click="resetPassword(user.id)">重置密码</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

interface User {
  id: number
  username: string
  phone: string
  address: string
  role: string
  status: string
}

const router = useRouter()
const dialog: any = inject('dialog')
const api = axios.create({ baseURL: 'http://localhost:8080/api' })

const users = ref<User[]>([])
const currentFilter = ref('all')

const filters = [
  { value: 'all', label: '全部' },
  { value: 'customer', label: '顾客' },
  { value: 'merchant', label: '商家' },
  { value: 'admin', label: '管理员' }
]

const filteredUsers = computed(() => {
  if (currentFilter.value === 'all') return users.value
  return users.value.filter(u => u.role === currentFilter.value)
})

onMounted(async () => {
  await fetchUsers()
})

async function fetchUsers() {
  try {
    const res = await api.get('/admin/users')
    users.value = res.data
  } catch (err) {
    dialog?.value?.show('错误', '加载用户列表失败')
  }
}

async function updateRole(userId: number, newRole: string) {
  const confirmed = await dialog?.value?.confirm('确认修改', '确定要修改该用户的角色吗？')
  if (!confirmed) return

  try {
    await api.put(`/admin/users/${userId}/role`, { role: newRole })
    dialog?.value?.show('成功', '角色已更新')
  } catch (err) {
    dialog?.value?.show('失败', '更新角色失败')
    await fetchUsers() // 刷新恢复原值
  }
}

async function toggleStatus(userId: number, currentStatus: string) {
  const newStatus = currentStatus === 'active' ? 'disabled' : 'active'
  const action = newStatus === 'active' ? '启用' : '禁用'
  const confirmed = await dialog?.value?.confirm('确认操作', `确定要${action}该用户吗？`)
  if (!confirmed) return

  try {
    await api.put(`/admin/users/${userId}/status`, { status: newStatus })
    dialog?.value?.show('成功', `${action}成功`)
    await fetchUsers()
  } catch (err) {
    dialog?.value?.show('失败', `${action}失败`)
  }
}

async function resetPassword(userId: number) {
  const confirmed = await dialog?.value?.confirm('重置密码', '确定要将密码重置为 123456 吗？')
  if (!confirmed) return

  try {
    await api.put(`/admin/users/${userId}/password`, { password: '123456' })
    dialog?.value?.show('成功', '密码已重置为 123456')
  } catch (err) {
    dialog?.value?.show('失败', '重置密码失败')
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.admin-users {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  padding: 8px 12px;
  border-radius: 32px;
  cursor: pointer;
  color: var(--text-primary);
  transition: background 0.2s;
}

.back-button:hover {
  background: var(--bg-secondary);
}

.back-icon {
  font-size: 20px;
}

.back-text {
  font-size: 14px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: var(--text-primary);
}

.filter-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 6px 16px;
  border: 1px solid var(--border-color);
  border-radius: 32px;
  background: var(--bg-card);
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

.users-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 16px;
  flex-wrap: wrap;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
}

.user-info {
  flex: 2;
  min-width: 150px;
}

.user-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.user-phone {
  font-size: 12px;
  color: var(--text-muted);
}

.user-address {
  font-size: 12px;
  color: var(--text-secondary);
}

.user-status {
  min-width: 60px;
}

.status-badge {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 20px;
}

.status-active {
  background: #e8f5e9;
  color: #2e7d32;
}

[data-theme="dark"] .status-active {
  background: #1b3a1b;
  color: #81c784;
}

.status-disabled {
  background: #ffebee;
  color: #c62828;
}

[data-theme="dark"] .status-disabled {
  background: #3a1a1a;
  color: #ef9a9a;
}

.user-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.role-select {
  padding: 6px 12px;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  background: var(--bg-input);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 12px;
}

.btn-status, .btn-reset {
  padding: 6px 12px;
  border: none;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-disable {
  background: #ffebee;
  color: #c62828;
}

.btn-disable:hover {
  background: #ef9a9a;
  color: white;
}

.btn-enable {
  background: #e8f5e9;
  color: #2e7d32;
}

.btn-enable:hover {
  background: #81c784;
  color: white;
}

.btn-reset {
  background: #fff3e0;
  color: #f57c00;
}

.btn-reset:hover {
  background: #ffb74d;
  color: white;
}

[data-theme="dark"] .btn-disable {
  background: #3a1a1a;
  color: #ef9a9a;
}

[data-theme="dark"] .btn-enable {
  background: #1b3a1b;
  color: #81c784;
}

[data-theme="dark"] .btn-reset {
  background: #3a2a00;
  color: #ffb74d;
}
</style>
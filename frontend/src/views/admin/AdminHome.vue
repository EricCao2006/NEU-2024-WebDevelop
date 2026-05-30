<template>
  <div class="admin-home">
    <div class="page-header">
      <h2>👑 管理员后台</h2>
    </div>

    <div class="admin-cards">
      <div class="admin-card" @click="goTo('/admin/users')">
        <div class="card-icon">👥</div>
        <div class="card-title">用户管理</div>
      </div>

      <!-- 一键恢复按钮 -->
      <div class="admin-card danger-card" @click="resetDatabase">
        <div class="card-icon">🔄</div>
        <div class="card-title">一键恢复</div>
        <div class="card-desc">重置数据库到初始状态</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import axios from 'axios'
import { inject } from 'vue'

const router = useRouter()
const dialog: any = inject('dialog')
const api = axios.create({ baseURL: 'http://localhost:8080/api' })

function goTo(path: string) {
  router.push(path)
}

async function resetDatabase() {
  const confirmed = await dialog?.value?.confirm('危险操作', '一键恢复将清空所有数据并重置到初始状态，确定要继续吗？')
  if (confirmed) {
    try {
      const res = await api.post('/admin/reset')
      if (res.data.success) {
        dialog?.value?.show('恢复成功', '数据库已重置，请重新登录')
        setTimeout(() => {
          localStorage.removeItem('user')
          router.push('/login')
        }, 1500)
      } else {
        dialog?.value?.show('恢复失败', res.data.message)
      }
    } catch (err) {
      dialog?.value?.show('恢复失败', '网络错误')
    }
  }
}
</script>

<style scoped>
.admin-home {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.admin-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.admin-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 200px;
}

.admin-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.danger-card {
  border-color: var(--danger-color);
}

.danger-card:hover {
  border-color: var(--danger-color);
  background: rgba(211, 47, 47, 0.05);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.card-desc {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
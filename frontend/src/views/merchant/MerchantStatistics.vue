<template>
  <div class="merchant-statistics">
    <!-- 页面头部 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon"><</span>
        <span class="back-text">返回</span>
      </button>
      <h2 class="page-title">📊 销售统计</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-info">
          <div class="stat-value">¥{{ totalSales }}</div>
          <div class="stat-label">总销售额</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalQuantity }}</div>
          <div class="stat-label">总销售量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📋</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalOrders }}</div>
          <div class="stat-label">总订单数</div>
        </div>
      </div>
    </div>

    <!-- 销售额排行 -->
    <div class="rank-section">
      <h3>🏆 销售额排行</h3>
      <div class="rank-list">
        <div v-for="(item, index) in salesRank" :key="item.productId" class="rank-item">
          <div class="rank-num" :class="getRankClass(index)">{{ index + 1 }}</div>
          <div class="rank-name">{{ item.productName }}</div>
          <div class="rank-value">¥{{ item.totalSales }}</div>
        </div>
        <div v-if="salesRank.length === 0" class="empty-rank">暂无销售数据</div>
      </div>
    </div>

    <!-- 销售量排行 -->
    <div class="rank-section">
      <h3>📊 销售量排行</h3>
      <div class="rank-list">
        <div v-for="(item, index) in quantityRank" :key="item.productId" class="rank-item">
          <div class="rank-num" :class="getRankClass(index)">{{ index + 1 }}</div>
          <div class="rank-name">{{ item.productName }}</div>
          <div class="rank-value">{{ item.totalQuantity }}件</div>
        </div>
        <div v-if="quantityRank.length === 0" class="empty-rank">暂无销售数据</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useUser } from '@/composables/useUser.ts'

const router = useRouter()
const dialog: any = inject('dialog')
const api = axios.create({ baseURL: 'http://localhost:8080/api' })
const { user } = useUser()

const totalSales = ref(0)
const totalQuantity = ref(0)
const totalOrders = ref(0)
const salesRank = ref<any[]>([])
const quantityRank = ref<any[]>([])

onMounted(async () => {
  await loadStatistics()
})

async function loadStatistics() {
  if (!user.value) return

  try {
    // 获取统计汇总
    const summaryRes = await api.get(`/statistics/summary/${user.value.userId}`)
    totalSales.value = summaryRes.data.totalSales || 0
    totalQuantity.value = summaryRes.data.totalQuantity || 0
    totalOrders.value = summaryRes.data.totalOrders || 0

    // 获取销售额排行
    const salesRes = await api.get(`/statistics/sales-rank/${user.value.userId}`)
    salesRank.value = salesRes.data

    // 获取销售量排行
    const quantityRes = await api.get(`/statistics/quantity-rank/${user.value.userId}`)
    quantityRank.value = quantityRes.data
  } catch (err) {
    dialog?.value?.show('错误', '加载统计数据失败')
  }
}

function getRankClass(index: number) {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return ''
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.merchant-statistics {
  max-width: 800px;
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

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 36px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

/* 排行区域 */
.rank-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 24px;
}

.rank-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: var(--text-primary);
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.rank-num {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: 600;
  background: var(--bg-card);
  color: var(--text-secondary);
}

.rank-first {
  background: #ffd700;
  color: #333;
}

.rank-second {
  background: #c0c0c0;
  color: #333;
}

.rank-third {
  background: #cd7f32;
  color: white;
}

.rank-name {
  flex: 1;
  font-weight: 500;
  color: var(--text-primary);
}

.rank-value {
  font-weight: 600;
  color: var(--primary-color);
}

.empty-rank {
  text-align: center;
  padding: 30px;
  color: var(--text-muted);
}
</style>
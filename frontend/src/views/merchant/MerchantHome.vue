<template>
  <div class="merchant-home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        👋 欢迎回来，<span class="username">{{ user?.username }}</span>
        <span class="role-badge merchant">🏪 商家</span>
      </div>
      <div class="welcome-address">
        📦 店铺地址：{{ user?.address || '未设置' }}
        <router-link to="/profile">✏️ 修改</router-link>
      </div>
    </div>

    <!-- 双栏布局 -->
    <div class="dashboard">
      <!-- 左侧：2x2 正方形菜单 -->
      <div class="sidebar-grid">
        <div class="menu-square" @click="goTo('/merchant/products')">
          <div class="menu-icon">🏪</div>
          <div class="menu-title">商品管理</div>
        </div>
        <div class="menu-square" @click="goTo('/merchant/stock')">
          <div class="menu-icon">📦</div>
          <div class="menu-title">进货管理</div>
        </div>
        <div class="menu-square" @click="goTo('/merchant/orders')">
          <div class="menu-icon">📋</div>
          <div class="menu-title">订单处理</div>
        </div>
        <div class="menu-square" @click="goTo('/merchant/statistics')">
          <div class="menu-icon">📊</div>
          <div class="menu-title">销售统计</div>
        </div>
      </div>

      <!-- 右侧：可切换看板 -->
      <div class="dashboard-panel">
        <div class="panel-header">
          <button class="nav-btn" @click="prevPanel" :disabled="currentPanel === 0">◀</button>
          <h3 class="panel-title">{{ panelTitle }}</h3>
          <button class="nav-btn" @click="nextPanel" :disabled="currentPanel === panels.length - 1">▶</button>
        </div>

        <div class="panel-content">
          <!-- 销售额排行 -->
          <div v-if="currentPanel === 0" class="rank-list">
            <div v-for="(item, index) in salesRank" :key="item.productId" class="rank-item">
              <div class="rank-num" :class="getRankClass(index)">{{ index + 1 }}</div>
              <div class="rank-name">{{ item.productName }}</div>
              <div class="rank-value">¥{{ item.totalSales }}</div>
            </div>
            <div v-if="salesRank.length === 0" class="empty-panel">暂无销售数据</div>
          </div>

          <!-- 销售量排行 -->
          <div v-else-if="currentPanel === 1" class="rank-list">
            <div v-for="(item, index) in quantityRank" :key="item.productId" class="rank-item">
              <div class="rank-num" :class="getRankClass(index)">{{ index + 1 }}</div>
              <div class="rank-name">{{ item.productName }}</div>
              <div class="rank-value">{{ item.totalQuantity }}件</div>
            </div>
            <div v-if="quantityRank.length === 0" class="empty-panel">暂无销售数据</div>
          </div>

          <!-- 近期新品 -->
          <div v-else-if="currentPanel === 2" class="new-products">
            <div v-for="product in newProducts" :key="product.id" class="new-product-item">
              <div class="new-product-icon">📦</div>
              <div class="new-product-info">
                <div class="new-product-name">{{ product.name }}</div>
                <div class="new-product-price">¥{{ product.minPrice }}起</div>
                <div class="new-product-time">{{ formatDate(product.createTime) }}</div>
              </div>
            </div>
            <div v-if="newProducts.length === 0" class="empty-panel">暂无新品</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const api = axios.create({ baseURL: 'http://localhost:8080/api' })

const user = ref<any>(null)

// 看板数据
const salesRank = ref<any[]>([])
const quantityRank = ref<any[]>([])
const newProducts = ref<any[]>([])

// 当前面板索引
const currentPanel = ref(0)
const panels = ['销售额排行', '销售量排行', '近期新品']
const panelTitle = computed(() => panels[currentPanel.value])

onMounted(async () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
  }
  await loadDashboardData()
})

async function loadDashboardData() {
  if (!user.value) return

  try {
    const salesRes = await api.get(`/statistics/sales-rank/${user.value.userId}`)
    salesRank.value = salesRes.data

    const quantityRes = await api.get(`/statistics/quantity-rank/${user.value.userId}`)
    quantityRank.value = quantityRes.data

    const newRes = await api.get(`/statistics/new-products/${user.value.userId}`)
    newProducts.value = newRes.data
  } catch (err) {
    console.error('加载数据失败:', err)
  }
}

function goTo(path: string) {
  router.push(path)
}

function prevPanel() {
  if (currentPanel.value > 0) {
    currentPanel.value--
  }
}

function nextPanel() {
  if (currentPanel.value < panels.length - 1) {
    currentPanel.value++
  }
}

function getRankClass(index: number) {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return ''
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}
</script>

<style scoped>
.merchant-home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.welcome-banner {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 20px 24px;
  margin-bottom: 32px;
}

.welcome-text {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.username {
  color: var(--primary-color);
}

.role-badge {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 24px;
  font-weight: 500;
}

.role-badge.merchant {
  background: var(--warning-light);
  color: var(--warning-color);
}

.welcome-address {
  font-size: 14px;
  color: var(--text-secondary);
}

.welcome-address a {
  margin-left: 8px;
  color: var(--primary-color);
}

.dashboard {
  display: grid;
  grid-template-columns: 1fr 2fr;  /* 左右等宽 1:1 */
  gap: 24px;
}

@media (max-width: 768px) {
  .dashboard {
    grid-template-columns: 1fr;
  }
}

/* 左侧 2x2 正方形网格 */
.sidebar-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.menu-square {
  aspect-ratio: 1 / 1;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 16px;
}

.menu-square:hover {
  transform: translateY(-4px);
  border-color: var(--primary-light);
  box-shadow: var(--shadow-md);
}

.menu-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.menu-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
}

/* 右侧看板 */
.dashboard-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.nav-btn {
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-color);
  border-radius: 50%;
  background: var(--bg-card);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-primary);
  transition: all 0.2s;
}

.nav-btn:hover:not(:disabled) {
  background: var(--bg-secondary);
  border-color: var(--primary-color);
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.panel-content {
  padding: 20px;
  min-height: 350px;
}

/* 排行榜样式 */
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

/* 新品样式 */
.new-products {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.new-product-item {
  display: flex;
  gap: 16px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.new-product-icon {
  font-size: 32px;
}

.new-product-info {
  flex: 1;
}

.new-product-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.new-product-price {
  font-size: 14px;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.new-product-time {
  font-size: 12px;
  color: var(--text-muted);
}

.empty-panel {
  text-align: center;
  padding: 60px;
  color: var(--text-muted);
}
</style>
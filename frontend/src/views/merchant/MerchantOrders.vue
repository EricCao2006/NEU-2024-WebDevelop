<template>
  <div class="merchant-orders">
    <!-- 页面头部 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon"><</span>
        <span class="back-text">返回</span>
      </button>
      <h2 class="page-title">📋 订单处理</h2>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-tabs">
      <button
          v-for="status in filterOptions"
          :key="status.value"
          :class="['filter-btn', { active: currentFilter === status.value }]"
          @click="currentFilter = status.value"
      >
        {{ status.label }}
      </button>
    </div>

    <!-- 订单列表 -->
    <div v-if="orders.length === 0" class="empty-state">
      暂无订单
    </div>

    <div v-else class="orders-list">
      <div v-for="order in filteredOrders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-id">订单号：{{ order.id }}</div>
          <div class="order-status" :class="getStatusClass(order.orderStatus)">
            {{ getStatusText(order.orderStatus) }}
          </div>
        </div>

        <div class="order-body">
          <div class="order-info">
            <p><strong>商品：</strong>{{ order.productName }}</p>
            <p><strong>款式：</strong>{{ order.skuName }}</p>
            <p><strong>数量：</strong>{{ order.quantity }}</p>
            <p><strong>总价：</strong>¥{{ order.totalPrice }}</p>
            <p><strong>收货地址：</strong>{{ order.address }}</p>
            <p><strong>买家：</strong>{{ order.buyerName }}（{{ order.buyerPhone }}）</p>
            <p><strong>下单时间：</strong>{{ formatDate(order.orderTime) }}</p>
          </div>

          <div class="order-actions">
            <button
                v-if="order.orderStatus === 'pending'"
                class="btn-ship"
                @click="updateOrderStatus(order.id, 'shipped')"
            >
              发货
            </button>
            <button
                v-if="order.orderStatus === 'shipped'"
                class="btn-complete"
                @click="updateOrderStatus(order.id, 'delivered')"
            >
              确认完成
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useUser } from '@/composables/useUser.ts'

interface Order {
  id: number
  productId: number
  productName: string
  skuName: string
  quantity: number
  totalPrice: number
  orderStatus: string
  orderTime: string
  address: string
  buyerName: string
  buyerPhone: string
}

const router = useRouter()
const dialog: any = inject('dialog')
const api = axios.create({ baseURL: 'http://localhost:8080/api' })
const { user } = useUser()

const orders = ref<Order[]>([])
const currentFilter = ref('all')

const filterOptions = [
  { value: 'all', label: '全部' },
  { value: 'pending', label: '等待发货' },
  { value: 'shipped', label: '已发货' },
  { value: 'delivered', label: '已完成' }
]

function getStatusText(status: string) {
  switch (status) {
    case 'pending': return '等待发货'
    case 'shipped': return '已发货'
    case 'delivered': return '已完成'
    default: return status
  }
}

const filteredOrders = computed(() => {
  if (currentFilter.value === 'all') return orders.value
  return orders.value.filter(o => o.orderStatus === currentFilter.value)
})

onMounted(async () => {
  await fetchOrders()
})

async function fetchOrders() {
  if (!user.value) return
  try {
    const res = await api.get(`/orders/merchant/${user.value.userId}`)
    orders.value = res.data
  } catch (err) {
    dialog?.value?.show('错误', '加载订单失败')
  }
}

async function updateOrderStatus(orderId: number, status: string) {
  const actionText = status === 'shipped' ? '发货' : '确认完成'
  const confirmed = await dialog?.value?.confirm('确认操作', `确定要${actionText}吗？`)
  if (!confirmed) return

  try {
    await api.put(`/orders/${orderId}/status`, { orderStatus: status })
    dialog?.value?.show('成功', `${actionText}成功`)
    await fetchOrders()
  } catch (err) {
    dialog?.value?.show('失败', `${actionText}失败`)
  }
}

function getStatusClass(status: string) {
  switch (status) {
    case 'waiting_ship': return 'status-waiting'
    case 'shipped': return 'status-shipped'
    case 'delivered': return 'status-delivered'
    default: return ''
  }
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString()
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.merchant-orders {
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

.empty-state {
  text-align: center;
  padding: 60px;
  color: var(--text-muted);
  background: var(--bg-card);
  border-radius: 20px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.order-id {
  font-weight: 600;
  color: var(--text-primary);
}

.order-status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 20px;
}

.status-waiting {
  background: var(--warning-light);
  color: var(--warning-color);
}

.status-shipped {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.status-delivered {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.order-info {
  flex: 1;
}

.order-info p {
  margin: 8px 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.order-info p strong {
  color: var(--text-primary);
}

.order-actions {
  display: flex;
  gap: 8px;
}

.btn-ship, .btn-complete {
  padding: 8px 20px;
  border: none;
  border-radius: 32px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-ship {
  background: var(--primary-color);
  color: white;
}

.btn-ship:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.btn-complete {
  background: var(--warning-light);
  color: var(--warning-color);
}

.btn-complete:hover {
  background: var(--warning-color);
  color: white;
}
</style>
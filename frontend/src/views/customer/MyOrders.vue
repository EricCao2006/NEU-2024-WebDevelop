<template>
  <div>
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon"><</span>
        <span class="back-text">返回</span>
      </button>
      <h2 class="page-title">📋 我的订单</h2>
    </div>

    <div v-if="orders.length === 0" class="empty-orders">
      暂无订单，去逛逛吧
    </div>

    <div v-else>
      <div v-for="order in orders" :key="order.id" class="order-card">
        <p><strong>订单号：</strong>{{ order.id }}</p>
        <p><strong>商品：</strong>{{ order.productName }}</p>
        <p><strong>款式：</strong>{{ order.skuName }}</p>
        <p><strong>数量：</strong>{{ order.quantity }}</p>
        <p><strong>总价：</strong>¥{{ order.totalPrice }}</p>
        <p><strong>订单状态：</strong>
          <span :class="getStatusClass(order.orderStatus)">
            {{ getStatusText(order.orderStatus) }}
          </span>
        </p>
        <p><strong>下单时间：</strong>{{ formatDate(order.orderTime) }}</p>

        <!-- 退款/退货按钮 - 只有等待发货和已发货/已送达状态可申请 -->
        <div class="order-actions" v-if="canRefund(order.orderStatus)">
          <button class="btn-refund" @click="applyRefund(order.id)">
            {{ getRefundButtonText(order.orderStatus) }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

interface Order {
  id: number
  productId: number
  productName: string
  skuName: string
  quantity: number
  totalPrice: number
  orderStatus: string
  orderTime: string
}

const orders = ref<Order[]>([])

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString()
}

function getStatusText(status: string) {
  switch (status) {
    case 'waiting_ship': return '等待发货'
    case 'shipped': return '已发货'
    case 'delivered': return '签收成功'
    case 'refunding': return '退货中'
    case 'refunded': return '已退款'
    default: return status
  }
}

function getStatusClass(status: string) {
  switch (status) {
    case 'waiting_ship': return 'status-waiting'
    case 'shipped': return 'status-shipped'
    case 'delivered': return 'status-delivered'
    case 'refunding': return 'status-refunding'
    case 'refunded': return 'status-refunded'
    default: return ''
  }
}

function canRefund(status: string): boolean {
  return status === 'waiting_ship' || status === 'shipped' || status === 'delivered'
}

function getRefundButtonText(status: string): string {
  if (status === 'waiting_ship') return '申请退款'
  return '申请退货'
}

function goBack() {
  router.back()
}

async function applyRefund(orderId: number) {
  const order = orders.value.find(o => o.id === orderId)
  const buttonText = getRefundButtonText(order?.orderStatus || '')
  if (confirm(`确定要${buttonText}吗？`)) {
    try {
      const res = await axios.post(`http://localhost:8080/api/orders/${orderId}/refund`)
      if (res.data.success) {
        alert(`${buttonText}申请已提交`)
        await loadOrders()
      } else {
        alert(res.data.message || '申请失败')
      }
    } catch (err) {
      alert('申请失败，请稍后重试')
    }
  }
}

async function loadOrders() {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    alert('请先登录')
    return
  }
  const user = JSON.parse(userStr)
  const res = await axios.get(`http://localhost:8080/api/orders/user/${user.userId}`)
  orders.value = res.data
}

onMounted(async () => {
  await loadOrders()
})
</script>

<style scoped>
.empty-orders {
  text-align: center;
  padding: 50px;
  color: var(--text-muted);
  background: var(--bg-card);
  border-radius: 12px;
}

.order-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.order-card p {
  margin: 8px 0;
}

.status-waiting {
  color: var(--warning-color);
}

.status-shipped {
  color: var(--primary-color);
}

.status-delivered {
  color: var(--primary-color);
}

.status-refunding {
  color: var(--danger-color);
}

.status-refunded {
  color: var(--text-muted);
}

.order-actions {
  margin-top: 16px;
}

.btn-refund {
  padding: 6px 16px;
  border: none;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--warning-light);
  color: var(--warning-color);
}

.btn-refund:hover {
  background: var(--warning-color);
  color: white;
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
</style>
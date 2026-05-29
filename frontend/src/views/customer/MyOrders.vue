<template>
  <div>
    <h2>我的订单</h2>
    <router-link to="/" class="back-link">← 返回商城</router-link>

    <div v-if="orders.length === 0" class="empty-orders">
      暂无订单，去逛逛吧
    </div>

    <div v-else>
      <div v-for="order in orders" :key="order.id" class="order-card">
        <p><strong>订单号：</strong>{{ order.id }}</p>
        <p><strong>商品ID：</strong>{{ order.productId }}</p>
        <p><strong>数量：</strong>{{ order.quantity }}</p>
        <p><strong>总价：</strong>¥{{ order.totalPrice }}</p>
        <p><strong>状态：</strong>
          <span :class="order.status === 'completed' ? 'status-completed' : 'status-pending'">
            {{ order.status === 'completed' ? '已完成' : '待处理' }}
          </span>
        </p>
        <p><strong>下单时间：</strong>{{ formatDate(order.orderTime) }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

interface Order {
  id: number
  productId: number
  quantity: number
  totalPrice: number
  status: string
  orderTime: string
}

const orders = ref<Order[]>([])

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString()
}

onMounted(async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    alert('请先登录')
    return
  }
  const user = JSON.parse(userStr)
  const res = await axios.get(`http://localhost:8080/api/orders/user/${user.userId}`)
  orders.value = res.data
})
</script>

<style scoped>
.back-link {
  display: inline-block;
  margin-bottom: 20px;
  color: var(--primary-color);
}

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

.status-completed {
  color: var(--primary-color);
}

.status-pending {
  color: var(--warning-color);
}
</style>
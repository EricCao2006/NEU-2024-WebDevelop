<template>
  <div>
    <!-- 未登录时显示 -->
    <div v-if="!user" class="login-prompt">
      <p>请先 <router-link to="/login">登录</router-link></p>
    </div>

    <!-- 已登录时显示完整内容 -->
    <div v-else>
      <!-- 欢迎横幅 -->
      <div class="welcome-banner">
        <div class="welcome-text">
          👋 欢迎回来，<span class="username">{{ user.username }}</span>
          <span v-if="user.role === 'merchant'" class="role-badge merchant">🏪 商家</span>
          <span v-if="user.role === 'admin'" class="role-badge admin">👑 管理员</span>
        </div>
        <div class="welcome-address">
          📦 收货地址：{{ user.address || '未设置' }}
          <router-link to="/profile">✏️ 修改</router-link>
        </div>
      </div>

      <!-- 商品网格 -->
      <div class="products-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <div class="product-image">📦</div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-price">¥{{ product.price }}</p>
            <p class="product-stock">库存：{{ product.stock }}</p>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-actions">
              <input type="number" v-model.number="quantities[product.id]" class="quantity-input" />
              <button @click="buy(product.id)" class="btn btn-primary btn-round">立即购买</button>
              <button @click="addToCart(product.id)" class="btn btn-warning btn-round">加入购物车</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址弹窗 -->
    <AddressDialog v-model:visible="showAddressDialog" @confirm="handleAddressConfirm" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import AddressDialog from '../../components/AddressDialog.vue'

interface Product {
  id: number
  name: string
  price: number
  stock: number
  description: string
}

interface User {
  userId: number
  username: string
  address: string
  role: string
  phone: string
}

const router = useRouter()
const route = useRoute()
const api = axios.create({ baseURL: 'http://localhost:8080/api' })

const products = ref<Product[]>([])
const user = ref<User | null>(null)
const quantities = ref<Record<number, number>>({})
const showAddressDialog = ref(false)
const pendingOrder = ref<{ productId: number; quantity: number; totalPrice: number } | null>(null)

// 注入自定义弹窗
const dialog: any = inject('dialog')

onMounted(async () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
  }
  await fetchProducts()
})

async function fetchProducts() {
  const keyword = route.query.keyword as string
  let url = '/products'
  if (keyword) {
    url = `/products/search?keyword=${encodeURIComponent(keyword)}`
  }
  const res = await api.get(url)
  products.value = res.data
  products.value.forEach(p => {
    quantities.value[p.id] = 1
  })
}

async function buy(productId: number) {
  if (!user.value) {
    dialog?.value?.show('提示', '请先登录')
    router.push('/login')
    return
  }

  const quantity = quantities.value[productId] ?? 1
  const product = products.value.find(p => p.id === productId)
  if (!product) return
  const totalPrice = product.price * quantity

  // 检查地址
  if (!user.value.address || user.value.address.trim() === '') {
    pendingOrder.value = { productId, quantity, totalPrice }
    showAddressDialog.value = true
    return
  }

  await doBuy(productId, quantity, totalPrice)
}

async function doBuy(productId?: number, quantity?: number, totalPrice?: number) {
  let finalProductId = productId
  let finalQuantity = quantity
  let finalTotalPrice = totalPrice

  if (pendingOrder.value) {
    finalProductId = pendingOrder.value.productId
    finalQuantity = pendingOrder.value.quantity
    finalTotalPrice = pendingOrder.value.totalPrice
    pendingOrder.value = null
  }

  if (!finalProductId || !finalQuantity || !finalTotalPrice) return

  try {
    const res = await api.post('/orders', {
      userId: user.value!.userId,
      productId: finalProductId,
      quantity: finalQuantity,
      totalPrice: finalTotalPrice
    })
    if (res.data.success) {
      dialog?.value?.show('下单成功', '✅ 订单已提交')
    } else {
      dialog?.value?.show('下单失败', '❌ 请稍后重试')
    }
  } catch (err) {
    dialog?.value?.show('下单失败', '网络错误，请稍后重试')
  }
}

async function handleAddressConfirm(address: string) {
  if (!user.value) return

  // 更新后端地址
  await api.put(`/users/${user.value.userId}/address`, { address })
  // 更新本地存储
  user.value.address = address
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const u = JSON.parse(userStr)
    u.address = address
    localStorage.setItem('user', JSON.stringify(u))
  }

  // 继续下单
  await doBuy()
}

function addToCart(productId: number) {
  const product = products.value.find(p => p.id === productId)
  if (!product) return

  const cart = localStorage.getItem('cart')
  let cartItems = cart ? JSON.parse(cart) : []

  const existing = cartItems.find((item: any) => item.id === productId)
  if (existing) {
    existing.quantity++
  } else {
    cartItems.push({
      id: product.id,
      name: product.name,
      price: product.price,
      quantity: 1
    })
  }

  localStorage.setItem('cart', JSON.stringify(cartItems))
  dialog?.value?.show('提示', '已加入购物车')
}
</script>

<style scoped>
.welcome-banner {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 20px 24px;
  margin-bottom: 28px;
}

.welcome-text {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.username {
  font-weight: 600;
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

.role-badge.admin {
  background: rgba(25, 118, 210, 0.15);
  color: #42a5f5;
}

.welcome-address {
  font-size: 14px;
  color: var(--text-secondary);
}

.welcome-address a {
  margin-left: 8px;
  color: var(--primary-color);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

.product-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.product-image {
  height: 160px;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 52px;
}

.product-info {
  padding: 16px 20px 20px;
  background: var(--bg-card);
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.product-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 6px;
}

.product-stock {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 10px;
}

.product-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  line-height: 1.4;
}

.product-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.quantity-input {
  width: 60px;
  padding: 8px 0;
  text-align: center;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  color: var(--text-primary);
}

.btn {
  padding: 8px 16px;
  border: none;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-round {
  border-radius: 32px;
}

.btn-primary {
  background: var(--primary-color);
  color: var(--bg-card);
}

.btn-primary:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.btn-warning {
  background: var(--warning-light);
  color: var(--warning-color);
}

.btn-warning:hover {
  background: var(--warning-color);
  color: var(--bg-card);
}
</style>
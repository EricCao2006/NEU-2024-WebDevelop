<template>
  <div>
    <!-- 欢迎横幅 -->
    <div v-if="user" class="welcome-banner">
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
          <p class="product-price">¥{{ product.minPrice }}起</p>
          <p class="product-stock">库存：{{ product.totalStock }}件</p>
          <p class="product-desc">{{ product.shortDescription }}</p>
          <div class="product-actions">
            <div class="action-row">
              <button @click="goToDetail(product.id)" class="btn-outline">详情</button>
              <button @click="addToCart(product)" class="btn-warning">加入购物车</button>
              <button @click="buy(product)" class="btn-primary">购买</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址弹窗 -->
    <AddressDialog v-model:visible="showAddressDialog" @confirm="handleAddressConfirm" />

    <!-- 商品选择弹窗 -->
    <ProductSelectDialog
        v-model:visible="showSelectDialog"
        :product="selectedProduct"
        @confirm="handleProductConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import AddressDialog from '../../components/AddressDialog.vue'
import ProductSelectDialog from '../../components/ProductSelectDialog.vue'
import { useUser } from '@/composables/useUser.ts'

interface Product {
  id: number
  name: string
  shortDescription: string
  category: string
  imageUrl: string
  userId: number
  minPrice: number
  totalStock: number
}

const router = useRouter()
const route = useRoute()
const dialog: any = inject('dialog')
const api = axios.create({ baseURL: 'http://localhost:8080/api' })

const products = ref<Product[]>([])
const showAddressDialog = ref(false)
const showSelectDialog = ref(false)
const selectedProduct = ref<Product | null>(null)
const pendingOrder = ref<{ productId: number; skuId: number; quantity: number; totalPrice: number } | null>(null)
let pendingAction: 'cart' | 'buy' = 'cart'

// 使用全局用户状态 - user 是 ref，所以要用 user.value
const { user, updateUserAddress } = useUser()

onMounted(async () => {
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
}

function goToDetail(productId: number) {
  router.push(`/product/${productId}`)
}

function openSelectDialog(product: Product, action: 'cart' | 'buy') {
  selectedProduct.value = product
  pendingAction = action
  showSelectDialog.value = true
}

function addToCart(product: Product) {
  if (!user.value) {
    dialog?.value?.show('提示', '请先登录')
    router.push('/login')
    return
  }
  openSelectDialog(product, 'cart')
}

function buy(product: Product) {
  if (!user.value) {
    dialog?.value?.show('提示', '请先登录')
    router.push('/login')
    return
  }
  openSelectDialog(product, 'buy')
}

function handleProductConfirm(data: { skuId: number; skuName: string; price: number; quantity: number }) {
  if (pendingAction === 'cart') {
    const cart = localStorage.getItem('cart')
    let cartItems = cart ? JSON.parse(cart) : []

    const existing = cartItems.find((item: any) => item.skuId === data.skuId)
    if (existing) {
      existing.quantity += data.quantity
    } else {
      cartItems.push({
        productId: selectedProduct.value?.id,
        productName: selectedProduct.value?.name,
        skuId: data.skuId,
        skuName: data.skuName,
        price: data.price,
        quantity: data.quantity
      })
    }
    localStorage.setItem('cart', JSON.stringify(cartItems))
    dialog?.value?.show('提示', `已添加 ${data.quantity} 件商品到购物车`)
  } else {
    if (!user.value?.address) {
      pendingOrder.value = {
        productId: selectedProduct.value!.id,
        skuId: data.skuId,
        quantity: data.quantity,
        totalPrice: data.price * data.quantity
      }
      showAddressDialog.value = true
      return
    }

    api.post('/orders', {
      userId: user.value.userId,
      productId: selectedProduct.value?.id,
      skuId: data.skuId,
      skuName: data.skuName,
      quantity: data.quantity,
      totalPrice: data.price * data.quantity
    }).then(res => {
      if (res.data.success) {
        dialog?.value?.show('下单成功', '订单已提交')
        router.push('/orders')
      } else {
        dialog?.value?.show('下单失败', '请稍后重试')
      }
    }).catch(() => {
      dialog?.value?.show('下单失败', '网络错误')
    })
  }
}

async function handleAddressConfirm(address: string) {
  if (!user.value) return

  await api.put(`/users/${user.value.userId}/address`, { address })

  // 使用全局方法更新地址
  updateUserAddress(address)

  if (pendingOrder.value) {
    await api.post('/orders', {
      userId: user.value.userId,
      productId: pendingOrder.value.productId,
      quantity: pendingOrder.value.quantity,
      totalPrice: pendingOrder.value.totalPrice
    })
    dialog?.value?.show('下单成功', '订单已提交')
    router.push('/orders')
    pendingOrder.value = null
  }
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
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-actions {
  margin-top: 12px;
}

.action-row {
  display: flex;
  gap: 8px;
}

.action-row button {
  flex: 1;
  padding: 8px 0;
  font-size: 13px;
  font-weight: 500;
  border: none;
  border-radius: 32px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-outline {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
}

.btn-outline:hover {
  background: var(--bg-secondary);
  transform: translateY(-1px);
}

.btn-primary {
  background: var(--primary-color);
  color: white;
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
  color: white;
  transform: translateY(-1px);
}
</style>
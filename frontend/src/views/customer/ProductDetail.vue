<template>
  <div class="detail-container">

    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon"><</span>
        <span class="back-text">返回</span>
      </button>
    </div>

    <!-- 商品头部：左侧图片，右侧信息 -->
    <div class="product-header">
      <div class="product-image">
        <div class="image-placeholder">📦</div>
      </div>
      <div class="product-header-info">
        <h1 class="product-name">{{ product?.name }}</h1>
        <div class="product-shop">
          <span class="shop-icon">🏪</span>
          <span class="shop-name">{{ shopName }}</span>
        </div>
        <div class="product-ship-from">发货地：{{ shipFrom }}</div>
        <div class="product-price" v-if="product?.minPrice">
          ¥{{ product.minPrice }}起
        </div>
      </div>
    </div>

    <!-- 商品详情描述 -->
    <div class="product-description">
      <h3>商品详情</h3>
      <p class="product-description-text">{{ product?.longDescription || product?.shortDescription }}</p>
    </div>

    <!-- 固定底栏 -->
    <div class="fixed-bottom-bar">
      <div class="price-section">
        <span class="price-label">价格</span>
        <span class="price-value">¥{{ selectedPrice || product?.minPrice }}起</span>
      </div>
      <div class="button-group">
        <button class="btn-warning btn-small" @click="openSelectDialog('cart')">加入购物车</button>
        <button class="btn-primary btn-small" @click="openSelectDialog('buy')">立即购买</button>
      </div>
    </div>

    <!-- 商品选择弹窗 -->
    <ProductSelectDialog
        v-model:visible="showSelectDialog"
        :product="product"
        @confirm="handleProductConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import ProductSelectDialog from '../../components/ProductSelectDialog.vue'

interface Product {
  id: number
  name: string
  shortDescription: string
  longDescription: string
  category: string
  imageUrl: string
  userId: number
  minPrice: number
  totalStock: number
  skus?: any[]
}

interface User {
  userId: number
  username: string
  address: string
  role: string
  phone: string
}

interface Merchant {
  id: number
  username: string
  address: string
  phone: string
}

const route = useRoute()
const router = useRouter()
const dialog: any = inject('dialog')
const api = axios.create({ baseURL: 'http://localhost:8080/api' })

const product = ref<Product | null>(null)
const user = ref<User | null>(null)
const merchant = ref<Merchant | null>(null)
const showSelectDialog = ref(false)
let pendingAction: 'cart' | 'buy' = 'cart'
const selectedPrice = ref(0)

const shopName = computed(() => merchant.value?.username || '官方旗舰店')
const shipFrom = computed(() => merchant.value?.address || '中国大陆')

onMounted(async () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
  }

  const productId = route.params.id
  if (productId) {
    await fetchProduct(productId as string)
  }
})

async function fetchProduct(id: string) {
  try {
    const res = await api.get(`/products/${id}`)
    product.value = res.data
    if (product.value?.userId) {
      await fetchMerchant(product.value.userId)
    }
  } catch (err) {
    console.error('获取商品失败:', err)
    dialog?.value?.show('错误', '商品不存在')
    router.push('/')
  }
}

async function fetchMerchant(userId: number) {
  try {
    const res = await api.get(`/users/${userId}`)
    merchant.value = res.data
  } catch (err) {
    console.error('获取商家信息失败:', err)
  }
}

function openSelectDialog(action: 'cart' | 'buy') {
  if (!user.value) {
    dialog?.value?.show('提示', '请先登录')
    router.push('/login')
    return
  }
  pendingAction = action
  showSelectDialog.value = true
}

function goBack() {
  router.back()
}

function handleProductConfirm(data: { skuId: number; skuName: string; price: number; quantity: number }) {
  selectedPrice.value = data.price

  if (pendingAction === 'cart') {
    const cart = localStorage.getItem('cart')
    let cartItems = cart ? JSON.parse(cart) : []

    const existing = cartItems.find((item: any) => item.skuId === data.skuId)
    if (existing) {
      existing.quantity += data.quantity
    } else {
      cartItems.push({
        productId: product.value?.id,
        productName: product.value?.name,
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
      dialog?.value?.show('提示', '请先在个人中心填写收货地址')
      router.push('/profile')
      return
    }

    api.post('/orders', {
      userId: user.value!.userId,
      productId: product.value?.id,
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
</script>

<style scoped>
.detail-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px 20px 80px;
}

.product-header {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  background: var(--bg-card);
  border-radius: 24px;
  padding: 20px;
}

.product-image {
  flex-shrink: 0;
  width: 130px;
  height: 130px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  background: var(--bg-secondary);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 52px;
}

.product-header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.3;
}

.product-shop {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.shop-icon {
  font-size: 14px;
}

.shop-name {
  font-size: 13px;
  color: var(--primary-color);
}

.product-ship-from {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  margin-top: 8px;
}

.product-description {
  background: var(--bg-card);
  border-radius: 24px;
  padding: 20px;
}

.product-description h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.product-description-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.fixed-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);
  padding: 12px 20px;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-label {
  font-size: 13px;
  color: var(--text-muted);
}

.price-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
}

.button-group {
  display: flex;
  gap: 12px;
}

.btn-small {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  border-radius: 32px;
  cursor: pointer;
  transition: all 0.2s ease;
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

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.detail-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px 20px 80px;
}

.page-header {
  margin-bottom: 20px;
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

</style>
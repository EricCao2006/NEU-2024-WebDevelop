<template>
  <div>
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon"><</span>
        <span class="back-text">返回</span>
      </button>
      <h2 class="page-title">🛒 购物车</h2>
    </div>

    <div v-if="cartItems.length === 0" class="empty-cart">
      购物车是空的，去逛逛吧
    </div>

    <div v-else>
      <div v-for="(item, index) in cartItems" :key="index" class="cart-item">
        <div class="cart-item-info">
          <h3>{{ item.productName }}</h3>
          <p class="sku-name">{{ item.skuName }}</p>
          <p>¥{{ item.price }} × {{ item.quantity }}</p>
          <p class="cart-item-subtotal">小计：¥{{ item.price * item.quantity }}</p>
        </div>
        <div class="cart-item-actions">
          <button @click="increaseQuantity(index)" class="btn">+</button>
          <span class="cart-quantity">{{ item.quantity }}</span>
          <button @click="decreaseQuantity(index)" class="btn">-</button>
          <button @click="removeItem(index)" class="btn btn-danger">删除</button>
        </div>
      </div>

      <div class="cart-footer">
        <h3>总计：¥{{ totalPrice }}</h3>
        <button @click="checkout" class="btn btn-primary checkout-btn">去结算</button>
      </div>
    </div>

    <!-- 地址弹窗 -->
    <AddressDialog v-model:visible="showAddressDialog" @confirm="handleAddressConfirm" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import AddressDialog from '../../components/AddressDialog.vue'

interface CartItem {
  productId: number
  productName: string
  skuId: number
  skuName: string
  price: number
  quantity: number
}

const router = useRouter()
const dialog: any = inject('dialog')
const cartItems = ref<CartItem[]>([])
const showAddressDialog = ref(false)
let pendingCheckoutUser: any = null

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

onMounted(() => {
  const cart = localStorage.getItem('cart')
  if (cart) {
    cartItems.value = JSON.parse(cart)
  }
})

function saveCart() {
  localStorage.setItem('cart', JSON.stringify(cartItems.value))
}

function increaseQuantity(index: number) {
  const item = cartItems.value[index]
  if (item) {
    item.quantity++
    saveCart()
  }
}

function decreaseQuantity(index: number) {
  const item = cartItems.value[index]
  if (item) {
    if (item.quantity > 1) {
      item.quantity--
    } else {
      cartItems.value.splice(index, 1)
    }
    saveCart()
  }
}

function removeItem(index: number) {
  cartItems.value.splice(index, 1)
  saveCart()
}

async function checkout() {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    dialog?.value?.show('提示', '请先登录')
    router.push('/login')
    return
  }

  const user = JSON.parse(userStr)

  if (!user.address || user.address.trim() === '') {
    pendingCheckoutUser = user
    showAddressDialog.value = true
    return
  }

  await doCheckout(user)
}

async function doCheckout(user: any) {
  for (const item of cartItems.value) {
    await axios.post('http://localhost:8080/api/orders', {
      userId: user.userId,
      productId: item.productId,
      skuId: item.skuId,
      skuName: item.skuName,
      quantity: item.quantity,
      totalPrice: item.price * item.quantity
    })
  }
  localStorage.removeItem('cart')
  cartItems.value = []
  dialog?.value?.show('下单成功', '✅ 订单已提交')
  router.push('/orders')
}

async function handleAddressConfirm(address: string) {
  const user = pendingCheckoutUser || JSON.parse(localStorage.getItem('user') || '{}')

  await axios.put(`http://localhost:8080/api/users/${user.userId}/address`, { address })
  user.address = address
  localStorage.setItem('user', JSON.stringify(user))

  pendingCheckoutUser = null
  await doCheckout(user)
}

function goBack() {
  router.back()
}
</script>

<style scoped>
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

.empty-cart {
  text-align: center;
  padding: 50px;
  color: var(--text-muted);
  background: var(--bg-card);
  border-radius: 12px;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.cart-item-info {
  flex: 1;
}

.cart-item-info h3 {
  margin-bottom: 4px;
}

.sku-name {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.cart-item-subtotal {
  color: var(--primary-color);
  font-size: 18px;
  font-weight: bold;
  margin-top: 8px;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cart-quantity {
  min-width: 30px;
  text-align: center;
}

.cart-footer {
  border-top: 2px solid var(--border-color);
  padding: 20px;
  text-align: right;
  margin-top: 20px;
}

.checkout-btn {
  font-size: 16px;
  padding: 10px 24px;
  margin-left: 16px;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 32px;
  cursor: pointer;
  font-size: 14px;
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-danger {
  background: var(--danger-color);
  color: white;
  border-radius: 32px;
}
</style>
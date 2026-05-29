<template>
  <header class="app-header">
    <div class="header-left">
      <router-link to="/" class="logo">🛒 线上商城</router-link>
    </div>

    <div class="header-center">
      <div class="search-box">
        <input
            type="text"
            v-model="keyword"
            placeholder="搜索商品..."
            class="search-input"
            @keyup.enter="handleSearch"
        />
        <button @click="handleSearch" class="search-btn">🔍</button>
      </div>
    </div>

    <div class="header-right">
      <router-link v-if="user" to="/cart" class="nav-icon">
        🛒 <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
        <span class="nav-text">购物车</span>
      </router-link>

      <router-link v-if="user" to="/orders" class="nav-icon">
        📋 <span class="nav-text">我的订单</span>
      </router-link>

      <button @click="toggleTheme" class="theme-btn">
        {{ theme === 'light' ? '🌙' : '☀️' }}
      </button>

      <div v-if="user" class="user-menu" @click="toggleMenu">
        <div class="user-avatar">
          {{ user.username?.charAt(0) || '👤' }}
        </div>
        <div v-show="menuVisible" class="dropdown-menu">
          <div class="dropdown-item" @click="goToProfile">📄 个人中心</div>

          <!-- 普通用户专属 -->
          <div v-if="user?.role === 'user'" class="dropdown-item" @click="goToOrders">📋 我的订单</div>

          <!-- 商家专属菜单 -->
          <div v-if="user?.role === 'merchant'" class="dropdown-divider"></div>
          <div v-if="user?.role === 'merchant'" class="dropdown-item" @click="goToMerchantProducts">
            🏪 商品管理
          </div>
          <div v-if="user?.role === 'merchant'" class="dropdown-item" @click="goToMerchantOrders">
            📦 订单处理
          </div>
          <div v-if="user?.role === 'merchant'" class="dropdown-item" @click="goToMerchantStatistics">
            📊 销售统计
          </div>

          <!-- 管理员专属菜单 -->
          <div v-if="user?.role === 'admin'" class="dropdown-divider"></div>
          <div v-if="user?.role === 'admin'" class="dropdown-item" @click="goToAdminUsers">
            👥 用户管理
          </div>

          <div class="dropdown-divider"></div>
          <div class="dropdown-item" @click="handleLogout">🚪 退出登录</div>
        </div>
      </div>

      <div v-else class="login-links">
        <router-link to="/login" class="login-btn">登录</router-link>
        <router-link to="/register" class="register-btn">注册</router-link>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '../composables/useTheme'

const router = useRouter()
const keyword = ref('')
const menuVisible = ref(false)
const user = ref<{ username: string; role: string; userId: number } | null>(null)
const { theme, toggleTheme } = useTheme()

const cartCount = computed(() => {
  const cart = localStorage.getItem('cart')
  if (!cart) return 0
  const items = JSON.parse(cart)
  return items.reduce((sum: number, item: any) => sum + item.quantity, 0)
})

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
  }
  document.addEventListener('click', closeMenuOnClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenuOnClickOutside)
})

function toggleMenu(e: Event) {
  e.stopPropagation()
  menuVisible.value = !menuVisible.value
}

function closeMenuOnClickOutside() {
  menuVisible.value = false
}

function handleSearch() {
  if (keyword.value.trim()) {
    router.push(`/?keyword=${encodeURIComponent(keyword.value)}`)
  }
}

function goToProfile() {
  router.push('/profile')
  menuVisible.value = false
}

function goToMerchant() {
  router.push('/merchant')
  menuVisible.value = false
}

function goToAdmin() {
  router.push('/admin')
  menuVisible.value = false
}

function handleLogout() {
  localStorage.removeItem('user')
  localStorage.removeItem('cart')
  user.value = null
  menuVisible.value = false
  alert('已退出登录')
  router.push('/')
}

function goToMerchantProducts() {
  router.push('/merchant/products')
  menuVisible.value = false
}

function goToMerchantOrders() {
  router.push('/merchant/orders')
  menuVisible.value = false
}

function goToMerchantStatistics() {
  router.push('/merchant/statistics')
  menuVisible.value = false
}

function goToAdminUsers() {
  router.push('/admin/users')
  menuVisible.value = false
}

</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background-color: var(--header-bg);
  border-bottom: 1px solid var(--header-border);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: var(--primary-color);
  text-decoration: none;
}

.logo:hover {
  opacity: 0.8;
}

.header-center {
  flex: 1;
  max-width: 400px;
  margin: 0 20px;
}

.dropdown-divider {
  height: 1px;
  background-color: var(--border-color);
  margin: 8px 0;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 32px;
  overflow: hidden;
  transition: all 0.2s;
}

.search-box:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(46, 125, 50, 0.2);
}

.search-input {
  flex: 1;
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  outline: none;
  font-size: 14px;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-btn {
  padding: 8px 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-secondary);
}

.search-btn:hover {
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-icon {
  display: flex;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: var(--text-primary);
  font-size: 16px;
  padding: 6px 12px;
  border-radius: 32px;
  transition: background 0.2s;
  position: relative;
}

.nav-icon:hover {
  background: var(--bg-secondary);
}

.nav-text {
  font-size: 14px;
}

.cart-badge {
  position: absolute;
  top: -4px;
  left: 20px;
  background-color: var(--danger-color);
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 12px;
  min-width: 16px;
  text-align: center;
}

.theme-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  color: var(--text-primary);
  transition: background 0.2s;
}

.theme-btn:hover {
  background: var(--bg-secondary);
}

.user-menu {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.dropdown-menu {
  position: absolute;
  top: 48px;
  right: 0;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: var(--shadow-md);
  min-width: 140px;
  overflow: hidden;
  z-index: 100;
}

.dropdown-item {
  padding: 10px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
  color: var(--text-primary);
}

.dropdown-item:hover {
  background-color: var(--bg-secondary);
}

.dropdown-item.divider {
  border-top: 1px solid var(--border-color);
  padding: 0;
  margin: 4px 0;
}

.login-links {
  display: flex;
  gap: 12px;
}

.login-btn, .register-btn {
  padding: 8px 20px;
  border-radius: 32px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.login-btn {
  background: transparent;
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.login-btn:hover {
  background: var(--primary-color);
  color: white;
}

.register-btn {
  background: var(--primary-color);
  color: white;
}

.register-btn:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
}
</style>
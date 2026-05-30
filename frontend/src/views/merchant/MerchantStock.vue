<template>
  <div class="merchant-stock">
    <!-- 页面头部 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon"><</span>
        <span class="back-text">返回</span>
      </button>
      <h2 class="page-title">📦 进货管理</h2>
    </div>

    <!-- 两个按钮 -->
    <div class="stock-actions">
      <div class="stock-card" @click="goToRefill">
        <div class="stock-icon">📦</div>
        <div class="stock-title">已有商品补货</div>
      </div>
      <div class="stock-card" @click="goToAddProduct">
        <div class="stock-icon">✨</div>
        <div class="stock-title">添加新商品</div>
      </div>
    </div>

    <!-- 补货弹窗 -->
    <div v-if="refillDialogVisible" class="dialog-overlay" @click.self="closeRefillDialog">
      <div class="dialog-content">
        <h3>选择商品</h3>
        <select v-model="selectedProductId" class="input" @change="onProductChange">
          <option :value="0">请选择商品</option>
          <option v-for="product in products" :key="product.id" :value="product.id">
            {{ product.name }}
          </option>
        </select>

        <!-- 款式选择（只有选中商品后才显示） -->
        <select v-if="selectedProductId !== 0 && skus.length > 0" v-model="selectedSkuId" class="input">
          <option :value="0">请选择款式</option>
          <option v-for="sku in skus" :key="sku.id" :value="sku.id">
            {{ sku.skuName }} - 当前库存: {{ sku.stock }}件
          </option>
        </select>

        <input v-model.number="addStock" type="number" placeholder="补货数量" class="input" />
        <div class="dialog-buttons">
          <button @click="confirmRefill" class="btn-primary">确认补货</button>
          <button @click="closeRefillDialog" class="btn-outline">取消</button>
        </div>
      </div>
    </div>

    <!-- 添加新商品弹窗 -->
    <div v-if="addProductDialogVisible" class="dialog-overlay" @click.self="closeAddProductDialog">
      <div class="dialog-content">
        <h3>添加新商品</h3>
        <input v-model="newProduct.name" placeholder="商品名称" class="input" />
        <textarea v-model="newProduct.shortDescription" placeholder="商品简介" class="input" rows="2"></textarea>
        <textarea v-model="newProduct.longDescription" placeholder="商品详情" class="input" rows="3"></textarea>
        <input v-model="newProduct.category" placeholder="分类" class="input" />
        <input v-model.number="newProduct.price" type="number" placeholder="价格" class="input" />
        <input v-model.number="newProduct.stock" type="number" placeholder="库存" class="input" />
        <input v-model="newProduct.skuName" placeholder="款式名称（如：标准版）" class="input" />
        <div class="dialog-buttons">
          <button @click="confirmAddProduct" class="btn-primary">保存</button>
          <button @click="closeAddProductDialog" class="btn-outline">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

interface Product {
  id: number
  name: string
  shortDescription: string
  longDescription: string
  category: string
  minPrice: number
  totalStock: number
}

const router = useRouter()
const dialog: any = inject('dialog')
const api = axios.create({ baseURL: 'http://localhost:8080/api' })

const products = ref<Product[]>([])
const currentUser = ref<any>(null)

// 补货相关
const refillDialogVisible = ref(false)
const selectedProductId = ref(0)
const selectedSkuId = ref(0)
const addStock = ref<number | undefined>(undefined)
const skus = ref<any[]>([])

async function onProductChange() {
  selectedSkuId.value = 0
  if (selectedProductId.value === 0) {
    skus.value = []
    return
  }
  try {
    const res = await api.get(`/products/${selectedProductId.value}/skus`)
    skus.value = res.data
  } catch (err) {
    console.error('获取款式失败:', err)
    skus.value = []
  }
}

// 添加新商品相关
const addProductDialogVisible = ref(false)
const newProduct = ref({
  name: '',
  shortDescription: '',
  longDescription: '',
  category: '',
  price: undefined as number | undefined,
  stock: undefined as number | undefined,
  skuName: '标准版'
})

onMounted(async () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
  }
  await fetchProducts()
})

async function fetchProducts() {
  if (!currentUser.value) return
  const res = await api.get(`/products/merchant/${currentUser.value.userId}`)
  products.value = res.data
}

function goBack() {
  router.back()
}

function goToRefill() {
  if (products.value.length === 0) {
    dialog?.value?.show('提示', '暂无商品，请先添加新商品')
    return
  }
  selectedProductId.value = 0
  selectedSkuId.value = 0
  addStock.value = undefined
  skus.value = []
  refillDialogVisible.value = true
}

async function confirmRefill() {
  if (selectedProductId.value === 0) {
    dialog?.value?.show('提示', '请选择商品')
    return
  }
  if (skus.value.length > 0 && selectedSkuId.value === 0) {
    dialog?.value?.show('提示', '请选择款式')
    return
  }
  if (!addStock.value || addStock.value <= 0) {
    dialog?.value?.show('提示', '请输入补货数量')
    return
  }

  try {
    await api.post('/products/refill', {
      productId: selectedProductId.value,
      skuId: selectedSkuId.value,
      quantity: addStock.value
    })
    dialog?.value?.show('成功', '补货成功')
    closeRefillDialog()
    await fetchProducts()
  } catch (err) {
    dialog?.value?.show('失败', '补货失败，请稍后重试')
  }
}

function closeRefillDialog() {
  refillDialogVisible.value = false
  selectedProductId.value = 0
  selectedSkuId.value = 0
  addStock.value = undefined
  skus.value = []
}

function goToAddProduct() {
  newProduct.value = {
    name: '',
    shortDescription: '',
    longDescription: '',
    category: '',
    price: undefined as number | undefined,
    stock: undefined as number | undefined,
    skuName: '标准版'
  }
  addProductDialogVisible.value = true
}

async function confirmAddProduct() {
  if (!newProduct.value.name) {
    dialog?.value?.show('提示', '请填写商品名称')
    return
  }
  if (!newProduct.value.price || newProduct.value.price <= 0) {
    dialog?.value?.show('提示', '请填写价格')
    return
  }
  if (!newProduct.value.stock || newProduct.value.stock < 0) {
    dialog?.value?.show('提示', '请填写库存')
    return
  }

  try {
    const productRes = await api.post('/products/merchant', {
      name: newProduct.value.name,
      shortDescription: newProduct.value.shortDescription,
      longDescription: newProduct.value.longDescription,
      category: newProduct.value.category,
      userId: currentUser.value.userId
    })

    if (productRes.data.success) {
      const productId = productRes.data.id
      await api.post('/products/product-sku', {
        productId: productId,
        skuName: newProduct.value.skuName,
        price: newProduct.value.price,
        stock: newProduct.value.stock
      })
      dialog?.value?.show('成功', '商品添加成功')
      closeAddProductDialog()
      await fetchProducts()
    } else {
      dialog?.value?.show('失败', '添加商品失败')
    }
  } catch (err) {
    dialog?.value?.show('失败', '添加商品失败，请稍后重试')
  }
}

function closeAddProductDialog() {
  addProductDialogVisible.value = false
}
</script>

<style scoped>
.merchant-stock {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 48px;
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

.stock-actions {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stock-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 20px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stock-card:hover {
  transform: translateX(4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-light);
}

.stock-icon {
  font-size: 32px;
}

.stock-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  background: var(--bg-card);
  border-radius: 28px;
  padding: 24px;
  width: 90%;
  max-width: 400px;
  max-height: 85vh;
  overflow-y: auto;
}

.dialog-content h3 {
  margin-bottom: 20px;
  color: var(--text-primary);
}

.dialog-content .input {
  width: 100%;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  color: var(--text-primary);
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  font-family: inherit;
}

.dialog-content input.input {
  resize: none;
}

.dialog-content textarea.input {
  resize: none;
  min-height: 60px;
}

.dialog-content .input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(46, 125, 50, 0.2);
}

.dialog-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-primary, .btn-outline {
  padding: 8px 20px;
  border: none;
  border-radius: 32px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.btn-outline {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
}

.btn-outline:hover {
  background: var(--bg-secondary);
}

select.input {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 16px;
}
</style>
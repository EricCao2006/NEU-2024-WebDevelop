<template>
  <div class="merchant-products">
    <!-- 页面头部 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon"><</span>
        <span class="back-text">返回</span>
      </button>
      <h2 class="page-title">🏪 商品管理</h2>
    </div>

    <!-- 商品列表 -->
    <div class="products-list">
      <div v-if="products.length === 0" class="empty-state">
        暂无商品，点击下方按钮前往进货
      </div>

      <div v-for="product in products" :key="product.id" class="product-item">
        <div class="product-icon">📦</div>
        <div class="product-info">
          <div class="product-name">{{ product.name }}</div>
          <div class="product-desc">{{ product.shortDescription }}</div>
          <div class="product-stats">
            <span class="product-price">¥{{ product.minPrice }}起</span>
            <span class="product-stock">库存：{{ product.totalStock }}件</span>
          </div>
        </div>
        <div class="product-actions">
          <button class="btn-edit" @click="editProduct(product)">编辑</button>
          <button class="btn-delete" @click="deleteProduct(product.id)">删除</button>
        </div>
      </div>
    </div>

    <!-- 底部固定按钮 - 跳转到进货 -->
    <div class="fixed-bottom-bar">
      <button class="btn-add" @click="goToStock">
        📦 进货管理
      </button>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog-content">
        <h3>{{ isEdit ? '编辑商品' : '新增商品' }}</h3>
        <input v-model="form.name" placeholder="商品名称" class="input" />
        <textarea v-model="form.shortDescription" placeholder="商品简介" class="input" rows="2"></textarea>
        <textarea v-model="form.longDescription" placeholder="商品详情" class="input" rows="3"></textarea>
        <input v-model="form.category" placeholder="分类" class="input" />
        <div class="dialog-buttons">
          <button @click="saveProduct" class="btn-primary">保存</button>
          <button @click="closeDialog" class="btn-outline">取消</button>
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
  imageUrl: string
  userId: number
  minPrice: number
  totalStock: number
}

const router = useRouter()
const dialog: any = inject('dialog')
const api = axios.create({ baseURL: 'http://localhost:8080/api' })

const products = ref<Product[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: 0,
  name: '',
  shortDescription: '',
  longDescription: '',
  category: ''
})

const currentUser = ref<any>(null)

onMounted(async () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
    console.log('当前商家:', currentUser.value)
  }
  await fetchProducts()
})

async function fetchProducts() {
  if (!currentUser.value) {
    console.log('没有用户信息')
    return
  }
  try {
    console.log('获取商品, userId:', currentUser.value.userId)
    const res = await api.get(`/products/merchant/${currentUser.value.userId}`)
    console.log('商品数据:', res.data)
    products.value = res.data
  } catch (err) {
    console.error('获取商品失败:', err)
  }
}

function goBack() {
  router.back()
}

function goToStock() {
  router.push('/merchant/stock')
}

function showAddDialog() {
  isEdit.value = false
  form.value = {
    id: 0,
    name: '',
    shortDescription: '',
    longDescription: '',
    category: ''
  }
  dialogVisible.value = true
}

function editProduct(product: Product) {
  isEdit.value = true
  form.value = {
    id: product.id,
    name: product.name,
    shortDescription: product.shortDescription,
    longDescription: product.longDescription,
    category: product.category
  }
  dialogVisible.value = true
}

async function saveProduct() {
  if (!form.value.name) {
    dialog?.value?.show('提示', '请填写商品名称')
    return
  }

  try {
    if (isEdit.value) {
      await api.put(`/products/merchant/${form.value.id}`, {
        ...form.value,
        userId: currentUser.value.userId
      })
      dialog?.value?.show('保存成功', '商品已更新')
    } else {
      await api.post('/products/merchant', {
        ...form.value,
        userId: currentUser.value.userId
      })
      dialog?.value?.show('保存成功', '商品已添加')
    }
    dialogVisible.value = false
    await fetchProducts()
  } catch (err) {
    dialog?.value?.show('保存失败', '请稍后重试')
  }
}

async function deleteProduct(id: number) {
  const confirmed = await dialog?.value?.confirm('确认删除', '确定要删除该商品吗？')
  if (confirmed) {
    await api.delete(`/products/merchant/${id}`)
    await fetchProducts()
    dialog?.value?.show('删除成功', '商品已删除')
  }
}

function closeDialog() {
  dialogVisible.value = false
}
</script>

<style scoped>
.merchant-products {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  padding-bottom: 80px;
}

/* 页面头部 */
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

/* 商品列表 */
.products-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 16px;
  transition: all 0.3s ease;
}

.product-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-light);
}

.product-icon {
  font-size: 48px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.product-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-stats {
  display: flex;
  gap: 16px;
}

.product-price {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
}

.product-stock {
  font-size: 14px;
  color: var(--text-muted);
}

.product-actions {
  display: flex;
  gap: 8px;
}

.btn-edit, .btn-delete {
  padding: 6px 16px;
  border: none;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-edit {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.btn-edit:hover {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.btn-delete {
  background: var(--danger-light);
  color: var(--danger-color);
}

.btn-delete:hover {
  background: var(--danger-color);
  color: white;
}

/* 底部固定按钮 */
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
  justify-content: center;
}

.btn-add {
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 32px;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 200px;
}

.btn-add:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

/* 弹窗 */
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
  resize: vertical;
  font-family: inherit;
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

.empty-state {
  text-align: center;
  padding: 60px;
  color: var(--text-muted);
  background: var(--bg-card);
  border-radius: 20px;
}
</style>
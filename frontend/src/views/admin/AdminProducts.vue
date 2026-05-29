<template>
  <div>
    <h2>后台管理 - 商品管理</h2>
    <router-link to="/" class="back-link">← 返回商城</router-link>

    <button @click="showAddDialog" class="btn btn-primary add-btn">+ 新增商品</button>

    <table class="data-table">
      <thead>
      <tr>
        <th>ID</th>
        <th>名称</th>
        <th>价格</th>
        <th>库存</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="product in products" :key="product.id">
        <td>{{ product.id }}</td>
        <td>{{ product.name }}</td>
        <td>¥{{ product.price }}</td>
        <td>{{ product.stock }}</td>
        <td>
          <button @click="editProduct(product)" class="btn">编辑</button>
          <button @click="deleteProduct(product.id)" class="btn btn-danger">删除</button>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- 弹窗 -->
    <div v-if="dialogVisible" class="dialog-overlay">
      <div class="dialog-content">
        <h3>{{ isEdit ? '编辑商品' : '新增商品' }}</h3>
        <input v-model="form.name" placeholder="商品名称" class="input" />
        <input v-model.number="form.price" type="number" placeholder="价格" class="input" />
        <input v-model.number="form.stock" type="number" placeholder="库存" class="input" />
        <textarea v-model="form.description" placeholder="描述" class="input" rows="3"></textarea>
        <input v-model="form.category" placeholder="分类" class="input" />
        <div class="dialog-buttons">
          <button @click="saveProduct" class="btn btn-primary">保存</button>
          <button @click="closeDialog" class="btn">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import axios from 'axios'

interface Product {
  id: number
  name: string
  price: number
  stock: number
  description: string
  category: string
}

const api = axios.create({ baseURL: 'http://localhost:8080/api' })
const dialog: any = inject('dialog')
const products = ref<Product[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<Product>({
  id: 0,
  name: '',
  price: 0,
  stock: 0,
  description: '',
  category: ''
})

onMounted(async () => {
  await fetchProducts()
})

async function fetchProducts() {
  const res = await api.get('/products')
  products.value = res.data
}

function showAddDialog() {
  isEdit.value = false
  form.value = { id: 0, name: '', price: 0, stock: 0, description: '', category: '' }
  dialogVisible.value = true
}

function editProduct(product: Product) {
  isEdit.value = true
  form.value = { ...product }
  dialogVisible.value = true
}

async function saveProduct() {
  try {
    if (isEdit.value) {
      await api.put(`/products/${form.value.id}`, form.value)
    } else {
      await api.post('/products', form.value)
    }
    dialogVisible.value = false
    await fetchProducts()
    dialog?.value?.show('保存成功', isEdit.value ? '商品已更新' : '商品已添加')
  } catch (err) {
    dialog?.value?.show('保存失败', '请稍后重试')
  }
}

async function deleteProduct(id: number) {
  const confirmed = await dialog?.value?.confirm('确认删除', '确定要删除该商品吗？')
  if (confirmed) {
    await api.delete(`/products/${id}`)
    await fetchProducts()
    dialog?.value?.show('删除成功', '商品已删除')
  }
}

function closeDialog() {
  dialogVisible.value = false
}
</script>

<style scoped>
.back-link {
  display: inline-block;
  margin-bottom: 20px;
  color: var(--primary-color);
}

.add-btn {
  margin-bottom: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  border: 1px solid var(--border-color);
  padding: 12px;
  text-align: left;
}

.data-table th {
  background-color: var(--bg-secondary);
  font-weight: 600;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog-content {
  background: var(--bg-card);
  padding: 24px;
  border-radius: 16px;
  width: 450px;
  max-width: 90%;
}

.dialog-content h3 {
  margin-bottom: 20px;
}

.dialog-content .input {
  margin-bottom: 12px;
}

.dialog-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
<template>
  <div v-if="visible" class="dialog-overlay" @click.self="close">
    <div class="dialog-container">
      <div class="dialog-header">
        <h3>选择商品</h3>
        <button class="close-btn" @click="close">✕</button>
      </div>

      <div class="dialog-body">
        <div class="product-info">
          <div class="product-image">📦</div>
          <div class="product-detail">
            <div class="product-name">{{ product?.name }}</div>
            <div class="product-price">¥{{ selectedSku?.price || product?.minPrice }}起</div>
          </div>
        </div>

        <!-- 款式选择 -->
        <div class="option-group">
          <div class="option-label">款式</div>
          <div class="option-buttons">
            <div
                v-for="sku in skuList"
                :key="sku.id"
                :class="['sku-item', { active: selectedSku?.id === sku.id }]"
                @click="selectSku(sku)"
            >
              <div class="sku-name">{{ sku.skuName }}</div>
              <div class="sku-price">¥{{ sku.price }}</div>
              <div class="sku-stock">库存 {{ sku.stock }}</div>
            </div>
          </div>
        </div>

        <!-- 数量选择 -->
        <div class="option-group">
          <div class="option-label">数量</div>
          <div class="quantity-selector">
            <button class="qty-btn" @click="decreaseQty">-</button>
            <span class="qty-value">{{ quantity }}</span>
            <button class="qty-btn" @click="increaseQty">+</button>
            <span class="stock-info">库存 {{ selectedSku?.stock || 0 }} 件</span>
          </div>
        </div>
      </div>

      <div class="dialog-footer">
        <button class="btn-primary btn-confirm" @click="confirm">
          确定
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import axios from 'axios'

interface Sku {
  id: number
  productId: number
  skuName: string
  price: number
  stock: number
}

interface Product {
  id: number
  name: string
  minPrice: number
  totalStock: number
}

const props = defineProps<{
  visible: boolean
  product: Product | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', data: { skuId: number; skuName: string; price: number; quantity: number }): void
}>()

const api = axios.create({ baseURL: 'http://localhost:8080/api' })

const skuList = ref<Sku[]>([])
const selectedSku = ref<Sku | null>(null)
const quantity = ref(1)

async function loadSkus() {
  if (!props.product) return
  try {
    const res = await api.get(`/products/${props.product.id}`)
    const skus = res.data.skus || []
    skuList.value = skus
    if (skus.length > 0) {
      selectedSku.value = skus[0]
    }
  } catch (err) {
    console.error('加载款式失败:', err)
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    quantity.value = 1
    loadSkus()
  }
})

function selectSku(sku: Sku) {
  selectedSku.value = sku
  quantity.value = 1
}

function increaseQty() {
  if (selectedSku.value && quantity.value < selectedSku.value.stock) {
    quantity.value++
  }
}

function decreaseQty() {
  if (quantity.value > 1) {
    quantity.value--
  }
}

function confirm() {
  if (!selectedSku.value) {
    alert('请选择款式')
    return
  }
  emit('confirm', {
    skuId: selectedSku.value.id,
    skuName: selectedSku.value.skuName,
    price: selectedSku.value.price,
    quantity: quantity.value
  })
  close()
}

function close() {
  emit('update:visible', false)
}
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--overlay);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.dialog-container {
  background: var(--bg-card);
  border-radius: 28px 28px 0 0;
  width: 100%;
  max-width: 500px;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 0 20px;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text-muted);
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: var(--text-primary);
}

.dialog-body {
  padding: 16px 20px;
  max-height: 60vh;
  overflow-y: auto;
}

/* 自定义滚动条 */
.dialog-body::-webkit-scrollbar {
  width: 6px;
}

.dialog-body::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 3px;
}

.dialog-body::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.dialog-body::-webkit-scrollbar-thumb:hover {
  background: var(--text-muted);
}

.product-info {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.product-image {
  width: 60px;
  height: 60px;
  background: var(--bg-secondary);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.product-detail {
  flex: 1;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
}

.option-group {
  margin-bottom: 20px;
}

.option-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.option-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sku-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--bg-input);
}

.sku-item:hover {
  border-color: var(--primary-color);
}

.sku-item.active {
  border-color: var(--primary-color);
  background: rgba(46, 125, 50, 0.05);
}

.sku-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.sku-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary-color);
}

.sku-stock {
  font-size: 12px;
  color: var(--text-muted);
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 16px;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--border-color);
  border-radius: 50%;
  background: var(--bg-input);
  font-size: 18px;
  cursor: pointer;
  color: var(--text-primary);
}

.qty-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.qty-value {
  font-size: 18px;
  font-weight: 600;
  min-width: 40px;
  text-align: center;
  color: var(--text-primary);
}

.stock-info {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: 8px;
}

.dialog-footer {
  padding: 16px 20px 24px;
}

.btn-confirm {
  width: 100%;
  padding: 14px 0;
  font-size: 16px;
  font-weight: 600;
  border-radius: 32px;
  background: var(--primary-color);
  color: white;
  border: none;
  cursor: pointer;
}

.btn-confirm:hover {
  background: var(--primary-hover);
}
</style>
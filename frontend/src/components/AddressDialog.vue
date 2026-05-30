<template>
  <div v-if="visible" class="dialog-overlay" @click.self="close">
    <div class="dialog-container">
      <div class="dialog-header">
        <h3>填写收货地址</h3>
      </div>
      <div class="dialog-body">
        <input
            v-model="address"
            placeholder="请输入收货地址"
            class="dialog-input"
            @keyup.enter="confirm"
        />
      </div>
      <div class="dialog-footer">
        <button @click="close" class="dialog-btn cancel">取消</button>
        <button @click="confirm" class="dialog-btn confirm">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', address: string): void
}>()

const address = ref('')

watch(() => props.visible, (val) => {
  if (!val) {
    address.value = ''
  }
})

function close() {
  emit('update:visible', false)
}

function confirm() {
  if (address.value.trim()) {
    emit('confirm', address.value)
    emit('update:visible', false)
  } else {
    alert('请输入地址')
  }
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
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.dialog-container {
  background: var(--bg-card);
  border-radius: 28px;
  width: 90%;
  max-width: 360px;
  box-shadow: 0 24px 38px rgba(0, 0, 0, 0.14), 0 9px 46px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  animation: dialogFadeIn 0.2s ease-out;
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.dialog-header {
  padding: 20px 24px 8px 24px;
}

.dialog-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: var(--text-primary);
}

.dialog-body {
  padding: 8px 24px;
}

.dialog-input {
  width: 100%;
  padding: 12px 16px;
  font-size: 16px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s;
}

.dialog-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(7, 193, 96, 0.2);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 24px 20px;
}

.dialog-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.dialog-btn.cancel {
  background: transparent;
  color: var(--text-secondary);
}

.dialog-btn.cancel:hover {
  background: var(--bg-secondary);
}

.dialog-btn.confirm {
  background: var(--primary-color);
  color: white;
}

.dialog-btn.confirm:hover {
  background: var(--primary-hover);
}
</style>
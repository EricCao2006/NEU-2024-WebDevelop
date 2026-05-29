<template>
  <div v-if="visible" class="dialog-overlay" @click.self="handleClose">
    <div class="dialog-container">
      <div class="dialog-header">
        <h3>{{ title }}</h3>
      </div>
      <div class="dialog-body">
        <p>{{ message }}</p>
      </div>
      <div class="dialog-footer" :class="{ 'dual-buttons': showCancel }">
        <button v-if="showCancel" @click="handleCancel" class="dialog-btn cancel">取消</button>
        <button @click="handleConfirm" class="dialog-btn confirm">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const visible = ref(false)
const title = ref('')
const message = ref('')
const showCancel = ref(false)
let confirmCallback: (() => void) | null = null
let cancelCallback: (() => void) | null = null

function show(t: string, msg: string): Promise<void> {
  return new Promise((resolve) => {
    title.value = t
    message.value = msg
    showCancel.value = false
    visible.value = true
    confirmCallback = resolve
    cancelCallback = null
  })
}

function confirm(t: string, msg: string): Promise<boolean> {
  return new Promise((resolve) => {
    title.value = t
    message.value = msg
    showCancel.value = true
    visible.value = true
    confirmCallback = () => resolve(true)
    cancelCallback = () => resolve(false)
  })
}

function handleConfirm() {
  visible.value = false
  if (confirmCallback) {
    confirmCallback()
    confirmCallback = null
  }
}

function handleCancel() {
  visible.value = false
  if (cancelCallback) {
    cancelCallback()
    cancelCallback = null
  }
}

function handleClose() {
  if (!showCancel.value) {
    handleConfirm()
  }
}

defineExpose({ show, confirm })
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
  backdrop-filter: blur(4px);
}

.dialog-container {
  background: var(--bg-card);
  border-radius: 28px;
  width: 90%;
  max-width: 320px;
  box-shadow: var(--shadow-lg);
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
  padding: 20px 20px 8px 20px;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.dialog-body {
  padding: 8px 20px;
}

.dialog-body p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px 20px;
  gap: 12px;
}

.dialog-footer.dual-buttons {
  justify-content: space-between;
}

.dialog-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.dialog-btn.confirm {
  background: var(--primary-color);
  color: white;
}

.dialog-btn.confirm:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.dialog-btn.cancel {
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.dialog-btn.cancel:hover {
  background: var(--border-color);
}
</style>
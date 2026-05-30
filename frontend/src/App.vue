<template>
  <div id="app">
    <AppHeader />
    <div class="main-container">
      <router-view />
    </div>
    <Dialog ref="dialogRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, provide, onMounted } from 'vue'
import AppHeader from './components/AppHeader.vue'
import Dialog from './components/Dialog.vue'
import { useUser } from './composables/useUser'

const dialogRef = ref()
provide('dialog', dialogRef)

const { loadUser } = useUser()

onMounted(() => {
  loadUser()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

@media (max-width: 768px) {
  .main-container {
    padding: 16px;
  }
}
</style>
<template>
  <div id="app">
    <AppHeader />
    <div style="max-width: 1200px; margin: 0 auto; padding: 24px">
      <router-view />
    </div>
    <Dialog ref="dialogRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, provide } from 'vue'
import AppHeader from './components/AppHeader.vue'
import Dialog from './components/Dialog.vue'

const dialogRef = ref()
provide('dialog', dialogRef)
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
</style>
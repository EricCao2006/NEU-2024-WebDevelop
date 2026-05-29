<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2 class="auth-title">注册</h2>
      <input v-model="username" placeholder="用户名" class="input" />
      <input v-model="phone" placeholder="手机号" class="input" />
      <input v-model="password" type="password" placeholder="密码" class="input" />
      <button @click="register" class="btn btn-primary btn-register">注册</button>
      <p class="auth-link">已有账号？<router-link to="/login">去登录</router-link></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const dialog: any = inject('dialog')
const phone = ref('')
const username = ref('')
const password = ref('')

async function register() {
  try {
    const res = await axios.post('http://localhost:8080/api/users/register', {
      phone: phone.value,
      username: username.value,
      password: password.value,
      address: '',
      role: 'user'
    })
    if (res.data.success) {
      dialog?.value?.show('注册成功', '请登录')
      setTimeout(() => {
        router.push('/login')
      }, 500)
    } else {
      dialog?.value?.show('注册失败', res.data.message || '手机号可能已注册')
    }
  } catch (err) {
    dialog?.value?.show('注册失败', '网络错误，请稍后重试')
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
}

.auth-card {
  max-width: 400px;
  width: 100%;
  padding: 32px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 32px;
  box-shadow: var(--shadow-md);
}

.auth-title {
  text-align: center;
  margin-bottom: 24px;
  color: var(--text-primary);
}

.input {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  color: var(--text-primary);
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  margin-bottom: 16px;
  box-sizing: border-box;
}

.input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(46, 125, 50, 0.2);
}

.input::placeholder {
  color: var(--text-muted);
}

.btn-register {
  width: 120px;
  padding: 10px 0;
  font-size: 16px;
  font-weight: 500;
  border-radius: 32px;
  margin: 8px auto 0;
  display: block;
  background: var(--primary-color);
  color: white;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-register:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.auth-link {
  text-align: center;
  margin-top: 16px;
  color: var(--text-secondary);
}
</style>
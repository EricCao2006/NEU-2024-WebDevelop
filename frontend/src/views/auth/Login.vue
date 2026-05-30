<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2 class="auth-title">登录</h2>
      <input v-model="phone" placeholder="手机号" class="input" />
      <input v-model="password" type="password" placeholder="密码" class="input" />
      <button @click="login" class="btn btn-primary btn-login">登录</button>
      <p class="auth-link">没有账号？<router-link to="/register">立即注册</router-link></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useUser } from '@/composables/useUser.ts'

const router = useRouter()
const phone = ref('')
const password = ref('')
const dialog: any = inject('dialog')
const { setUser } = useUser()

async function login() {
  try {
    const res = await axios.post('http://localhost:8080/api/users/login', null, {
      params: { phone: phone.value, password: password.value }
    })
    if (res.data.success) {
      const userData = {
        userId: res.data.userId,
        username: res.data.username,
        role: res.data.role,
        address: res.data.address,
        phone: res.data.phone,
        success: true
      }
      setUser(userData)
      dialog?.value?.show('登录成功', '欢迎回来！')
      setTimeout(() => {
        const role = res.data.role
        if (role === 'merchant') {
          router.push('/merchant')
        } else if (role === 'admin') {
          router.push('/admin')
        } else {
          router.push('/')
        }
      }, 500)
    } else {
      dialog?.value?.show('登录失败', res.data.message || '用户名或密码错误')
    }
  } catch (err) {
    dialog?.value?.show('登录失败', '网络错误，请稍后重试')
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

.btn-login {
  width: 120px;
  padding: 10px 0;
  font-size: 16px;
  font-weight: 500;
  border-radius: 32px;
  margin: 0 auto;
  display: block;
}


.auth-link {
  text-align: center;
  margin-top: 16px;
  color: var(--text-secondary);
}

.auth-test {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: var(--text-muted);
}
</style>
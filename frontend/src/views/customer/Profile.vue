<template>
  <div class="profile-container">
    <div class="profile-card">
      <h2>个人中心</h2>

      <!-- 头像行（头像 + 头像文字） -->
      <div class="avatar-row">
        <span class="avatar-label">头像</span>
        <div class="avatar" @click="editField('avatar')">
          {{ user?.username?.charAt(0) || '👤' }}
        </div>
      </div>

      <!-- 用户名 -->
      <div class="info-row">
        <label>用户名</label>
        <div class="info-value">
          <span>{{ user?.username }}</span>
        </div>
      </div>

      <!-- 修改个人资料按钮 -->
      <div class="edit-profile-btn">
        <button class="btn-primary btn-round" @click="editField('profile')">修改个人资料</button>
      </div>

      <!-- 手机号 -->
      <div class="info-row">
        <label>手机号</label>
        <div class="info-value">
          <span>{{ phoneDisplay }}</span>
          <button class="btn-link" @click="editField('phone')">改绑手机号</button>
        </div>
      </div>

      <!-- 修改密码按钮 -->
      <div class="edit-profile-btn">
        <button class="btn-danger btn-round" @click="editField('password')">修改密码</button>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="showEditDialog" class="dialog-overlay" @click.self="closeEditDialog">
      <div class="dialog-content">
        <h3>{{ editTitle }}</h3>

        <!-- 修改个人资料弹窗（头像 + 用户名） -->
        <div v-if="editFieldType === 'profile'" class="profile-edit-form">
          <div class="profile-edit-avatar" @click="triggerAvatarUpload">
            <div class="avatar-large">{{ tempAvatar || user?.username?.charAt(0) || '👤' }}</div>
            <span class="avatar-edit-hint">点击更换头像</span>
          </div>
          <input
              v-model="tempUsername"
              placeholder="新用户名"
              class="input"
          />
          <input
              type="file"
              ref="fileInput"
              style="display: none"
              accept="image/*"
              @change="handleAvatarUpload"
          />
        </div>

        <!-- 普通编辑（手机号/密码） -->
        <input
            v-else
            v-model="editValue"
            :type="editFieldType === 'password' ? 'password' : 'text'"
            :placeholder="editPlaceholder"
            class="input"
            @keyup.enter="saveEdit"
        />

        <div class="dialog-buttons">
          <button @click="saveEdit" class="btn-primary btn-round">保存</button>
          <button @click="closeEditDialog" class="btn-outline btn-round">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const dialog: any = inject('dialog')
const api = axios.create({ baseURL: 'http://localhost:8080/api' })

const user = ref<any>(null)
const showEditDialog = ref(false)
const editFieldType = ref('')
const editValue = ref('')
const editTitle = ref('')
const editPlaceholder = ref('')
let editCallback: ((value: string) => Promise<void>) | null = null

// 个人资料编辑专用
const tempUsername = ref('')
const tempAvatar = ref('')
const fileInput = ref<HTMLInputElement>()

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
  }
})

const phoneDisplay = computed(() => {
  if (!user.value?.phone) return '未绑定'
  const phone = user.value.phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

function editField(field: string) {
  editFieldType.value = field
  switch (field) {
    case 'profile':
      editTitle.value = '修改个人资料'
      tempUsername.value = user.value?.username || ''
      tempAvatar.value = ''
      break
    case 'phone':
      editTitle.value = '修改手机号'
      editPlaceholder.value = '请输入新手机号'
      editValue.value = user.value?.phone || ''
      editCallback = savePhone
      break
    case 'password':
      editTitle.value = '修改密码'
      editPlaceholder.value = '请输入新密码'
      editValue.value = ''
      editCallback = savePassword
      break
  }
  showEditDialog.value = true
}

function triggerAvatarUpload() {
  fileInput.value?.click()
}

function handleAvatarUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    tempAvatar.value = e.target?.result as string
  }
  reader.readAsDataURL(file)

  dialog?.value?.show('提示', '头像上传功能开发中')
}

async function saveProfile() {
  if (tempUsername.value && tempUsername.value !== user.value?.username) {
    await saveUsername(tempUsername.value)
  }
}

async function saveUsername(newUsername: string) {
  try {
    const res = await api.put(`/users/${user.value.userId}/username`, null, {
      params: { username: newUsername }
    })
    if (res.data.success) {
      user.value.username = newUsername
      updateLocalStorage()
      dialog?.value?.show('修改成功', '用户名已更新')
    } else {
      dialog?.value?.show('修改失败', res.data.message || '请稍后重试')
    }
  } catch (err) {
    dialog?.value?.show('修改失败', '网络错误')
  }
}

async function savePhone(newPhone: string) {
  if (!/^1[3-9]\d{9}$/.test(newPhone)) {
    dialog?.value?.show('手机号无效', '请输入11位手机号')
    return
  }
  try {
    const res = await api.put(`/users/${user.value.userId}/phone`, null, {
      params: { phone: newPhone }
    })
    if (res.data.success) {
      user.value.phone = newPhone
      updateLocalStorage()
      dialog?.value?.show('修改成功', '手机号已更新')
    } else {
      dialog?.value?.show('修改失败', res.data.message || '手机号可能已存在')
    }
  } catch (err) {
    dialog?.value?.show('修改失败', '网络错误')
  }
}

async function savePassword(newPassword: string) {
  if (newPassword.length < 6) {
    dialog?.value?.show('密码无效', '密码长度至少6位')
    return
  }
  try {
    const res = await api.put(`/users/${user.value.userId}/password`, null, {
      params: { password: newPassword }
    })
    if (res.data.success) {
      dialog?.value?.show('修改成功', '密码已更新，请重新登录')
      setTimeout(() => {
        localStorage.removeItem('user')
        router.push('/login')
      }, 1500)
    } else {
      dialog?.value?.show('修改失败', res.data.message || '请稍后重试')
    }
  } catch (err) {
    dialog?.value?.show('修改失败', '网络错误')
  }
}

function updateLocalStorage() {
  localStorage.setItem('user', JSON.stringify(user.value))
}

async function saveEdit() {
  if (editFieldType.value === 'profile') {
    await saveProfile()
  } else if (editCallback) {
    await editCallback(editValue.value)
  }
  closeEditDialog()
}

function closeEditDialog() {
  showEditDialog.value = false
  editValue.value = ''
  tempUsername.value = ''
  tempAvatar.value = ''
  editCallback = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
}

.profile-card {
  max-width: 500px;
  width: 100%;
  padding: 32px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 32px;
  box-shadow: var(--shadow-md);
}

.profile-card h2 {
  text-align: center;
  margin-bottom: 32px;
  color: var(--text-primary);
}

/* 头像行 */
.avatar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.avatar-label {
  font-weight: 500;
  color: var(--text-secondary);
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.avatar:hover {
  opacity: 0.8;
}

/* 信息行 */
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.info-row label {
  font-weight: 500;
  color: var(--text-secondary);
}

.info-value {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-primary);
}

/* 按钮样式 */
.btn-link {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  font-size: 14px;
  padding: 4px 8px;
  transition: opacity 0.2s;
}

.btn-link:hover {
  opacity: 0.7;
}

/* 两个主要按钮统一尺寸 */
.edit-profile-btn {
  margin: 24px 0;
  text-align: center;
}

.edit-profile-btn button {
  min-width: 160px;
  padding: 12px 24px;
  font-size: 16px;
  border: none;
  border-radius: 32px;
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

.btn-danger {
  background: var(--danger-color);
  color: white;
}

.btn-danger:hover {
  background: var(--danger-hover);
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

/* 弹窗内个人资料编辑样式 */
.profile-edit-form {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.profile-edit-avatar {
  cursor: pointer;
  text-align: center;
}

.avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 600;
  margin-bottom: 8px;
  transition: opacity 0.2s;
}

.avatar-large:hover {
  opacity: 0.8;
}

.avatar-edit-hint {
  font-size: 12px;
  color: var(--text-muted);
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
  max-width: 360px;
}

.dialog-content h3 {
  margin-bottom: 20px;
  color: var(--text-primary);
}

.dialog-content .input {
  width: 100%;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  color: var(--text-primary);
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.dialog-content .input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(46, 125, 50, 0.2);
}

.dialog-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
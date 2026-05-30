import { ref, readonly } from 'vue'

interface User {
    userId: number
    username: string
    role: string
    address: string
    phone: string
    success: boolean
}

// 使用 ref 让状态响应式
const user = ref<User | null>(null)

// 更新用户
function setUser(newUser: User | null) {
    user.value = newUser
    if (newUser) {
        localStorage.setItem('user', JSON.stringify(newUser))
    } else {
        localStorage.removeItem('user')
    }
}

// 更新用户地址
function updateUserAddress(address: string) {
    if (user.value) {
        user.value.address = address
        localStorage.setItem('user', JSON.stringify(user.value))
    }
}

// 从 localStorage 加载用户
function loadUser() {
    const userStr = localStorage.getItem('user')
    if (userStr) {
        user.value = JSON.parse(userStr)
    } else {
        user.value = null
    }
}

// 登出
function logout() {
    user.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('cart')
}

export function useUser() {
    return {
        user: readonly(user),
        setUser,
        updateUserAddress,
        loadUser,
        logout
    }
}
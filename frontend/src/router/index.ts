import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// 扩展路由元信息类型
declare module 'vue-router' {
    interface RouteMeta {
        roles?: string[]
    }
}

// 普通用户页面
import ProductList from '../views/customer/ProductList.vue'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import Cart from '../views/customer/Cart.vue'
import MyOrders from '../views/customer/MyOrders.vue'
import Profile from '../views/customer/Profile.vue'

// 商家页面
import MerchantProducts from '../views/merchant/MerchantProducts.vue'
import MerchantStatistics from '../views/merchant/MerchantStatistics.vue'
import MerchantOrders from '../views/merchant/MerchantOrders.vue'

// 管理员页面
import AdminUsers from '../views/admin/AdminUsers.vue'

const routes: RouteRecordRaw[] = [
    // 公共页面（无需登录）
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },

    // 普通用户页面
    { path: '/', name: 'ProductList', component: ProductList, meta: { roles: ['user', 'merchant', 'admin'] } },
    { path: '/cart', name: 'Cart', component: Cart, meta: { roles: ['user', 'merchant', 'admin'] } },
    { path: '/orders', name: 'Orders', component: MyOrders, meta: { roles: ['user'] } },  // 只有普通用户
    { path: '/profile', name: 'Profile', component: Profile, meta: { roles: ['user', 'merchant', 'admin'] } },

    // 商家页面
    { path: '/merchant/products', name: 'MerchantProducts', component: MerchantProducts, meta: { roles: ['merchant'] } },
    { path: '/merchant/statistics', name: 'MerchantStatistics', component: MerchantStatistics, meta: { roles: ['merchant'] } },
    { path: '/merchant/orders', name: 'MerchantOrders', component: MerchantOrders, meta: { roles: ['merchant'] } },

    // 管理员页面
    { path: '/admin/users', name: 'AdminUsers', component: AdminUsers, meta: { roles: ['admin'] } },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
    const userStr = localStorage.getItem('user')
    const publicPages = ['/login', '/register']
    const isPublicPage = publicPages.includes(to.path)

    if (!userStr && !isPublicPage) {
        next('/login')
        return
    }

    if (userStr && to.meta.roles) {
        const user = JSON.parse(userStr)
        if (!to.meta.roles.includes(user.role)) {
            next('/')
            return
        }
    }

    next()
})

export default router